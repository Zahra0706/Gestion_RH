package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.CongeService;
import com.example.gestion_rh.service.EmployeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/manager/conges")
@RequiredArgsConstructor
public class CongeRestController {

    private final CongeService congeService;
    private final EmployeService employeService;

    // ===== DTO (réponse propre pour Postman) =====
    public record CongeResponse(
            Long id,
            String dateDebut,
            String dateFin,
            String type,
            String statut,
            String motif,
            Long nombreJours,
            Long employeId,
            String employeNom,
            String commentaireManager,
            String commentaireRH
    ) {}

    // Body pour valider/refuser
    @Data
    public static class DecisionRequest {
        private String commentaire; // pour valider
        private String motifRefus;  // pour refuser
    }

    private Employe getManager(Authentication auth) {
        return employeService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    private CongeResponse toDto(Conge c) {
        String nom = (c.getEmploye() != null)
                ? (c.getEmploye().getPrenom() + " " + c.getEmploye().getNom())
                : null;
        Long empId = (c.getEmploye() != null) ? c.getEmploye().getId() : null;

        Long nbJours = (c.getDateDebut() != null && c.getDateFin() != null) ? c.getNombreJours() : null;

        return new CongeResponse(
                c.getId(),
                c.getDateDebut() != null ? c.getDateDebut().toString() : null,
                c.getDateFin() != null ? c.getDateFin().toString() : null,
                c.getTypeConge() != null ? c.getTypeConge().name() : null,
                c.getStatut() != null ? c.getStatut().name() : null,
                c.getMotif(),
                nbJours,
                empId,
                nom,
                c.getCommentaireManager(),
                c.getCommentaireRH()
        );
    }

    // ===== 0) Test =====
    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of("status", "OK", "message", "Backend REST fonctionne");
    }

    // ===== 1) Congés en attente (manager) =====
    @GetMapping("/en-attente")
    public List<CongeResponse> enAttente(Authentication auth) {
        Employe manager = getManager(auth);

        return congeService.getCongesEnAttenteManager(manager.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ===== 2) Congés validés (VALIDE_MANAGER + VALIDE_RH) =====
    @GetMapping("/valides")
    public List<CongeResponse> valides(Authentication auth) {
        Employe manager = getManager(auth);
        Long deptId = manager.getDepartement().getId();

        List<Conge> all = new ArrayList<>();
        all.addAll(congeService.getCongesDepartementByStatut(deptId, Conge.StatutConge.VALIDE_MANAGER));
        all.addAll(congeService.getCongesDepartementByStatut(deptId, Conge.StatutConge.VALIDE_RH));

        return all.stream().map(this::toDto).toList();
    }

    // ===== 3) Congés refusés (REFUSE_MANAGER) =====
    @GetMapping("/refuses")
    public List<CongeResponse> refuses(Authentication auth) {
        Employe manager = getManager(auth);
        Long deptId = manager.getDepartement().getId();

        return congeService.getCongesDepartementByStatut(deptId, Conge.StatutConge.REFUSE_MANAGER)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ===== 4) Historique (VALIDÉ + REFUSÉ + ANNULÉ...) =====
    @GetMapping("/historique")
    public List<CongeResponse> historique(Authentication auth) {
        Employe manager = getManager(auth);
        Long deptId = manager.getDepartement().getId();

        return congeService.getHistoriqueCongesDepartement(deptId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ===== 5) Stats (cartes en haut) =====
    @GetMapping("/stats")
    public Map<String, Object> stats(Authentication auth) {
        Employe manager = getManager(auth);
        Long deptId = manager.getDepartement().getId();

        List<Conge> enAttente = congeService.getCongesEnAttenteManager(manager.getId());

        List<Conge> valides = new ArrayList<>();
        valides.addAll(congeService.getCongesDepartementByStatut(deptId, Conge.StatutConge.VALIDE_MANAGER));
        valides.addAll(congeService.getCongesDepartementByStatut(deptId, Conge.StatutConge.VALIDE_RH));

        List<Conge> refuses = congeService.getCongesDepartementByStatut(deptId, Conge.StatutConge.REFUSE_MANAGER);

        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        long validesCeMois = valides.stream()
                .filter(c -> c.getDateDebut() != null && !c.getDateDebut().isBefore(debutMois))
                .count();

        return Map.of(
                "enAttente", enAttente.size(),
                "valides", valides.size(),
                "validesCeMois", validesCeMois,
                "refuses", refuses.size()
        );
    }

    // ===== 6) Calendrier =====
    @GetMapping("/calendrier")
    public List<Map<String, Object>> calendrier(Authentication auth) {
        Employe manager = getManager(auth);
        Long deptId = manager.getDepartement().getId();

        List<Conge> valides = new ArrayList<>();
        valides.addAll(congeService.getCongesDepartementByStatut(deptId, Conge.StatutConge.VALIDE_MANAGER));
        valides.addAll(congeService.getCongesDepartementByStatut(deptId, Conge.StatutConge.VALIDE_RH));

        return valides.stream()
                .filter(c -> c.getDateDebut() != null && c.getDateFin() != null && c.getEmploye() != null)
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    m.put("dateDebut", c.getDateDebut().toString());
                    m.put("dateFin", c.getDateFin().toString());
                    m.put("typeConge", c.getTypeConge() != null ? c.getTypeConge().name() : null);
                    m.put("statut", c.getStatut() != null ? c.getStatut().name() : null);
                    m.put("employeNom", c.getEmploye().getPrenom() + " " + c.getEmploye().getNom());
                    return m;
                })
                .toList();
    }

    // ===== 7) Valider une demande (POST) =====
    @PostMapping("/{id}/valider")
    public ResponseEntity<?> valider(
            @PathVariable Long id,
            @RequestBody(required = false) DecisionRequest body,
            Authentication auth
    ) {
        Employe manager = getManager(auth);
        String commentaire = (body != null) ? body.getCommentaire() : null;

        Conge updated = congeService.validerCongeManager(id, manager.getId(), true, commentaire);
        return ResponseEntity.ok(toDto(updated));
    }

    // ===== 8) Refuser une demande (POST) =====
    @PostMapping("/{id}/refuser")
    public ResponseEntity<?> refuser(
            @PathVariable Long id,
            @RequestBody DecisionRequest body,
            Authentication auth
    ) {
        if (body == null || body.getMotifRefus() == null || body.getMotifRefus().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "motifRefus est obligatoire"));
        }

        Employe manager = getManager(auth);
        Conge updated = congeService.validerCongeManager(id, manager.getId(), false, body.getMotifRefus());
        return ResponseEntity.ok(toDto(updated));
    }
}
