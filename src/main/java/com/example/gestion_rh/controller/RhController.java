package com.example.gestion_rh.controller;

import com.example.gestion_rh.dto.CongeStatsDTO;
import com.example.gestion_rh.dto.CongeValidationDTO;
import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.CongeService;
import com.example.gestion_rh.service.DepartementService;
import com.example.gestion_rh.service.EmailService;
import com.example.gestion_rh.service.EmployeService;
import com.example.gestion_rh.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/rh")
@RequiredArgsConstructor
public class RhController {

    private final EmployeService employeService;
    private final CongeService congeService;
    private final DepartementService departementService;
    private final StatistiqueService statistiqueService;
    private final EmailService emailService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = statistiqueService.getStatistiquesGenerales();
        model.addAllAttributes(stats);
        model.addAttribute("departements", departementService.getAllDepartements());

        // Add congés stats
        CongeStatsDTO congeStats = congeService.getStatistiques();
        model.addAttribute("congeStats", congeStats);

        return "rh/dashboard";
    }

    @GetMapping("/employes")
    public String getEmployes(Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departementId,
            @RequestParam(required = false) Employe.Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nom") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equals("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending());

        Page<Employe> employesPage = employeService.getEmployes(keyword, departementId, role, null, pageable);

        model.addAttribute("employesPage", employesPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employesPage.getTotalPages());
        model.addAttribute("totalItems", employesPage.getTotalElements());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);
        model.addAttribute("departementId", departementId);
        model.addAttribute("role", role);
        model.addAttribute("departements", departementService.getAllDepartements());
        model.addAttribute("roles", Employe.Role.values());

        return "rh/employes/liste";
    }

    // ==================== CONGÉS DASHBOARD ====================

    @GetMapping("/conges/dashboard")
    public String congesDashboard(Model model) {
        CongeStatsDTO stats = congeService.getStatistiques();
        model.addAttribute("stats", stats);

        // Top employees
        List<Object[]> topEmployees = congeService.getTopEmployeesByConges(LocalDate.now().getYear());
        model.addAttribute("topEmployees", topEmployees);

        // Monthly trends
        List<Object[]> tendances = congeService.getTendancesMensuelles(LocalDate.now().getYear());
        model.addAttribute("tendances", tendances);

        return "rh/conges/dashboard";
    }

    @GetMapping("/conges/dashboard/stats")
    @ResponseBody
    public ResponseEntity<CongeStatsDTO> getCongesStats() {
        return ResponseEntity.ok(congeService.getStatistiques());
    }

    // ==================== CONGÉS LIST & VALIDATION ====================

    @GetMapping("/conges")
    public String getConges(Model model) {
        model.addAttribute("congesEnAttente", congeService.getCongesEnAttenteRH());
        model.addAttribute("stats", congeService.getStatistiques());
        return "rh/conges/liste";
    }

    @GetMapping("/conges/en-attente")
    public String getCongesEnAttente(Model model,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long departementId,
            @RequestParam(required = false, defaultValue = "date") String sort) {
        List<Conge> conges = congeService.getCongesEnAttenteRH();

        // Filtering
        if (type != null && !type.isEmpty()) {
            conges = conges.stream()
                    .filter(c -> c.getTypeConge().name().equals(type))
                    .collect(Collectors.toList());
        }
        if (departementId != null) {
            conges = conges.stream()
                    .filter(c -> c.getEmploye().getDepartement() != null
                            && c.getEmploye().getDepartement().getId().equals(departementId))
                    .collect(Collectors.toList());
        }

        // Sorting
        if ("priorite".equals(sort)) {
            conges.sort((c1, c2) -> c1.getPriorite().compareTo(c2.getPriorite())); // Urgent first usually
        } else if ("departement".equals(sort)) {
            conges.sort((c1, c2) -> {
                String d1 = c1.getEmploye().getDepartement() != null ? c1.getEmploye().getDepartement().getNom() : "";
                String d2 = c2.getEmploye().getDepartement() != null ? c2.getEmploye().getDepartement().getNom() : "";
                return d1.compareTo(d2);
            });
        } else {
            // Default by date debut
            conges.sort((c1, c2) -> c1.getDateDebut().compareTo(c2.getDateDebut()));
        }

        model.addAttribute("conges", conges);
        model.addAttribute("stats", congeService.getStatistiques());
        model.addAttribute("departements", departementService.getAllDepartements());
        return "rh/conges/validation";
    }

    @GetMapping("/conges/historique")
    public String getCongesHistorique(Model model) {
        List<Conge> valides = congeService.getCongesByStatut(Conge.StatutConge.VALIDE_RH);
        List<Conge> refuses = congeService.getCongesByStatut(Conge.StatutConge.REFUSE_RH);

        List<Conge> historique = Stream.concat(valides.stream(), refuses.stream())
                .sorted((a, b) -> b.getDateTraitement() != null && a.getDateTraitement() != null
                        ? b.getDateTraitement().compareTo(a.getDateTraitement())
                        : 0)
                .collect(Collectors.toList());

        model.addAttribute("conges", historique);
        return "rh/conges/historique";
    }

    @GetMapping("/conges/{id}")
    public String getCongeDetails(@PathVariable Long id, Model model) {
        CongeValidationDTO validationData = congeService.getValidationData(id);
        model.addAttribute("data", validationData);
        model.addAttribute("conge", validationData.getConge());
        return "rh/conges/details";
    }

    @PostMapping("/conges/{id}/valider")
    public String validerConge(@PathVariable Long id,
            @RequestParam(required = false) String commentaire,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Long rhId = getCurrentUserId(auth);
            Conge conge = congeService.validerCongeRH(id, rhId, true,
                    commentaire != null ? commentaire : "Validé par RH");

            emailService.envoyerNotificationConge(
                    conge.getEmploye(),
                    "Votre demande de congé a été approuvée",
                    "Votre demande de congé du " + conge.getDateDebut() + " au " + conge.getDateFin()
                            + " a été approuvée.");

            redirectAttributes.addFlashAttribute("success", "Congé validé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/conges/en-attente";
    }

    @PostMapping("/conges/{id}/refuser")
    public String refuserConge(@PathVariable Long id,
            @RequestParam(required = false) String motifRefus,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Long rhId = getCurrentUserId(auth);
            Conge conge = congeService.validerCongeRH(id, rhId, false,
                    motifRefus != null ? motifRefus : "Refusé par RH");

            emailService.envoyerNotificationConge(
                    conge.getEmploye(),
                    "Votre demande de congé a été refusée",
                    "Votre demande de congé a été refusée. Motif: "
                            + (motifRefus != null ? motifRefus : "Non spécifié"));

            redirectAttributes.addFlashAttribute("success", "Congé refusé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/conges/en-attente";
    }

    @PostMapping("/conges/{id}/attente")
    public String mettreEnAttente(@PathVariable Long id,
            @RequestParam(required = false) String motif,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Long rhId = getCurrentUserId(auth);
            congeService.mettreEnAttenteRH(id, rhId, motif);
            redirectAttributes.addFlashAttribute("success", "Demande mise en attente d'informations");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/conges/en-attente";
    }

    @PostMapping("/conges/{id}/renvoyer")
    public String renvoyerAuManager(@PathVariable Long id,
            @RequestParam(required = false) String motif,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Long rhId = getCurrentUserId(auth);
            congeService.renvoyerAuManager(id, rhId, motif);
            redirectAttributes.addFlashAttribute("success", "Demande renvoyée au manager");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/conges/en-attente";
    }

    private Long getCurrentUserId(Authentication auth) {
        if (auth != null) {
            String email = auth.getName();
            return employeService.getEmployeByEmail(email)
                    .map(Employe::getId)
                    .orElse(1L);
        }
        return 1L;
    }

    // ==================== OTHER MODULES ====================

    @GetMapping("/evaluations")
    public String getEvaluations() {
        return "rh/evaluations/liste";
    }

    @GetMapping("/salaires")
    public String getSalaires() {
        return "rh/salaires/liste";
    }

    @GetMapping("/statistiques")
    public String getStatistiques(Model model) {
        Map<String, Object> stats = statistiqueService.getStatistiquesGenerales();
        model.addAllAttributes(stats);
        model.addAttribute("statistiquesParRole", statistiqueService.getStatistiquesParRole());

        List<Departement> departements = departementService.getAllDepartements();
        model.addAttribute("departements", departements);

        return "rh/statistiques";
    }

    // ==================== EXPORT & PARTAGE ====================

    private final com.example.gestion_rh.service.ExportService exportService;

    @GetMapping("/conges/export/excel")
    public ResponseEntity<byte[]> exportExcel() {
        List<Conge> conges = congeService.getAllConges();
        byte[] excelFile = exportService.generateCongesExcel(conges);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=conges.xlsx")
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .body(excelFile);
    }

    @GetMapping("/conges/export/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        List<Conge> conges = congeService.getAllConges();
        byte[] pdfFile = exportService.generateCongesPdf(conges);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=conges.pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfFile);
    }

    @PostMapping("/conges/partager")
    public String partagerRapport(RedirectAttributes redirectAttributes) {
        try {
            List<Conge> conges = congeService.getAllConges();
            byte[] pdfFile = exportService.generateCongesPdf(conges);

            // Send to direction
            emailService.envoyerRapport(
                    "direction@example.com",
                    "Rapport des Congés (Partage Manuel)",
                    "Veuillez trouver ci-joint le rapport des congés partagé manuellement.",
                    pdfFile,
                    "Rapport_Conges.pdf");

            redirectAttributes.addFlashAttribute("success", "Rapport partagé avec la direction avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du partage : " + e.getMessage());
        }
        return "redirect:/rh/conges";
    }
}
