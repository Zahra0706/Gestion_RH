package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.CongeService;
import com.example.gestion_rh.service.EmployeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ⚠️ CONTROLLER DE TEST UNIQUEMENT
 * À SUPPRIMER EN PRODUCTION
 * Permet de tester les fonctionnalités sans authentification
 */
@RestController
@RequestMapping("/api/test")
public class TestCongeController {

    private final CongeService congeService;
    private final EmployeService employeService;

    public TestCongeController(CongeService congeService, EmployeService employeService) {
        this.congeService = congeService;
        this.employeService = employeService;
    }

    /**
     * Endpoint de test pour valider un congé
     * POST http://localhost:8080/api/test/conges/1/valider
     * Body: managerEmail=manager@example.com&commentaire=OK
     */
    @PostMapping("/conges/{id}/valider")
    public ResponseEntity<?> validerCongeTest(
            @PathVariable Long id,
            @RequestParam(required = false) String commentaire,
            @RequestParam String managerEmail) {

        System.out.println("=== TEST VALIDATION CONGÉ ===");
        System.out.println("ID Congé: " + id);
        System.out.println("Commentaire: " + commentaire);
        System.out.println("Manager Email: " + managerEmail);

        try {
            Optional<Employe> managerOpt = employeService.findByEmail(managerEmail);

            if (managerOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Manager non trouvé", "email", managerEmail));
            }

            Employe manager = managerOpt.get();

            if (manager.getDepartement() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Manager sans département"));
            }

            Conge congeValide = congeService.validerCongeManager(
                    id,
                    manager.getId(),
                    true,
                    commentaire
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Congé validé avec succès");
            response.put("congeId", congeValide.getId());
            response.put("nouveauStatut", congeValide.getStatut().name());
            response.put("dateValidation", congeValide.getDateValidationManager());
            response.put("managerValidateur", manager.getNom() + " " + manager.getPrenom());

            System.out.println("✅ Validation réussie!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", e.getMessage(),
                            "type", e.getClass().getSimpleName()
                    ));
        }
    }

    /**
     * Endpoint pour lister les congés en attente
     * GET http://localhost:8080/api/test/conges/attente?managerEmail=manager@example.com
     */
    @GetMapping("/conges/attente")
    public ResponseEntity<?> getCongesEnAttente(@RequestParam String managerEmail) {
        try {
            Optional<Employe> managerOpt = employeService.findByEmail(managerEmail);

            if (managerOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Manager non trouvé"));
            }

            Employe manager = managerOpt.get();
            List<Conge> conges = congeService.getCongesEnAttenteManager(manager.getId());

            List<Map<String, Object>> result = conges.stream()
                    .map(c -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", c.getId());
                        m.put("employe", c.getEmploye().getNom() + " " + c.getEmploye().getPrenom());
                        m.put("dateDebut", c.getDateDebut());
                        m.put("dateFin", c.getDateFin());
                        m.put("nombreJours", c.getNombreJours());
                        m.put("type", c.getType());
                        m.put("statut", c.getStatut());
                        return m;
                    })
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "total", result.size(),
                    "conges", result
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint pour refuser un congé
     * POST http://localhost:8080/api/test/conges/1/refuser
     * Body: managerEmail=manager@example.com&motifRefus=Trop de demandes
     */
    @PostMapping("/conges/{id}/refuser")
    public ResponseEntity<?> refuserCongeTest(
            @PathVariable Long id,
            @RequestParam String motifRefus,
            @RequestParam String managerEmail) {

        System.out.println("=== TEST REFUS CONGÉ ===");
        System.out.println("ID Congé: " + id);
        System.out.println("Motif: " + motifRefus);

        try {
            Optional<Employe> managerOpt = employeService.findByEmail(managerEmail);

            if (managerOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Manager non trouvé"));
            }

            Employe manager = managerOpt.get();

            Conge congeRefuse = congeService.validerCongeManager(
                    id,
                    manager.getId(),
                    false,  // ❌ false = refus
                    motifRefus
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Congé refusé",
                    "congeId", congeRefuse.getId(),
                    "nouveauStatut", congeRefuse.getStatut().name(),
                    "motifRefus", congeRefuse.getMotifRefus()
            ));

        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}