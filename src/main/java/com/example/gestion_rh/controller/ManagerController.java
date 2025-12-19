package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.CongeRepository;
import com.example.gestion_rh.repository.DepartementRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.CongeService;
import com.example.gestion_rh.service.EmployeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final CongeRepository congeRepository;
    private final CongeService congeService;
    private final EmployeService employeService;

    public ManagerController(EmployeRepository employeRepository,
                             DepartementRepository departementRepository,
                             CongeRepository congeRepository,
                             CongeService congeService,
                             EmployeService employeService) {
        this.employeRepository = employeRepository;
        this.departementRepository = departementRepository;
        this.congeRepository = congeRepository;
        this.congeService = congeService;
        this.employeService = employeService;
    }

    // =========================
    // DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        model.addAttribute("emailConnecte", email);

        Optional<Employe> managerOpt = employeRepository.findByEmail(email);

        if (managerOpt.isEmpty()) {
            setEmptyDashboard(model);
            return "manager/dashboard";
        }

        Employe manager = managerOpt.get();

        Departement dep = manager.getDepartement();
        if (dep == null || dep.getId() == null) {
            setEmptyDashboard(model);
            return "manager/dashboard";
        }

        Long deptId = dep.getId();

        long congesEnAttente = congeRepository.countByDepartementAndStatut(
                deptId, Conge.StatutConge.EN_ATTENTE_MANAGER
        );

        long congesValides = congeRepository.countByDepartementAndStatut(
                deptId, Conge.StatutConge.VALIDE_MANAGER
        );

        long totalConges = congesEnAttente + congesValides;

        model.addAttribute("totalConges", totalConges);
        model.addAttribute("congesEnAttente", congesEnAttente);
        model.addAttribute("congesValides", congesValides);

        List<Employe> employesDept = employeRepository.findByDepartementId(deptId);

        long totalEmployes = employesDept.size();

        long employesActifs = employesDept.stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsCompteActif()))
                .count();

        long employesInactifs = employesDept.stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsCompteActif()))
                .count();

        model.addAttribute("totalEmployes", totalEmployes);
        model.addAttribute("employesActifs", employesActifs);
        model.addAttribute("employesInactifs", employesInactifs);

        Map<String, Long> statistiquesParRole = employesDept.stream()
                .filter(e -> e.getRole() != null)
                .collect(Collectors.groupingBy(e -> e.getRole().name(), Collectors.counting()));

        model.addAttribute("statistiquesParRole", statistiquesParRole);

        List<Employe> derniersEmployes = employesDept.stream()
                .sorted((a, b) -> {
                    if (a.getDateEmbauche() == null && b.getDateEmbauche() == null) return 0;
                    if (a.getDateEmbauche() == null) return 1;
                    if (b.getDateEmbauche() == null) return -1;
                    return b.getDateEmbauche().compareTo(a.getDateEmbauche());
                })
                .limit(5)
                .toList();

        model.addAttribute("derniersEmployes", derniersEmployes);

        List<Departement> departementsSansManager = departementRepository.findAll().stream()
                .filter(d -> d.getManager() == null)
                .limit(5)
                .toList();

        long totalDepartements = departementRepository.count();
        long departementsAvecManager = totalDepartements - departementsSansManager.size();

        int tauxOccupation = totalDepartements == 0 ? 0
                : (int) Math.round((departementsAvecManager * 100.0) / totalDepartements);

        model.addAttribute("totalDepartements", totalDepartements);
        model.addAttribute("departementsAvecManager", departementsAvecManager);
        model.addAttribute("tauxOccupation", tauxOccupation);
        model.addAttribute("departementsSansManager", departementsSansManager);

        return "manager/dashboard";
    }

    // =========================
    // ✅ GESTION DES CONGÉS
    // =========================
    @GetMapping("/conges")
    public String gestionConges(Authentication authentication, Model model) {
        Optional<Employe> managerOpt = employeService.findByEmail(authentication.getName());
        System.out.println("tis is anageopt"+
                managerOpt
        );
        if (managerOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Utilisateur non trouvé");
            setEmptyCongesData(model);
            return "manager/conges";
        }

        Employe manager = managerOpt.get();

        if (manager.getDepartement() == null) {
            model.addAttribute("errorMessage", "Vous n'êtes associé à aucun département");
            setEmptyCongesData(model);
            return "manager/conges";
        }

        Long departementId = manager.getDepartement().getId();

        // Congés en attente de validation
        List<Conge> congesEnAttenteList = congeService.getCongesEnAttenteManager(manager.getId());

        // Congés validés par le manager
        List<Conge> congesValidesList = congeService.getCongesDepartementByStatut(
                departementId,
                Conge.StatutConge.VALIDE_MANAGER
        );

        // Ajouter aussi les congés validés par RH
        List<Conge> congesValidesRH = congeService.getCongesDepartementByStatut(
                departementId,
                Conge.StatutConge.VALIDE_RH
        );
        congesValidesList.addAll(congesValidesRH);

        // Congés refusés
        List<Conge> congesRefusesList = congeService.getCongesDepartementByStatut(
                departementId,
                Conge.StatutConge.REFUSE_MANAGER
        );

        // Historique complet
        List<Conge> historiqueConges = congeService.getHistoriqueCongesDepartement(departementId);

        // Congés aujourd'hui
        LocalDate today = LocalDate.now();
        List<Conge> congesAujourdhui = congesValidesList.stream()
                .filter(c -> c.getDateDebut() != null && c.getDateFin() != null)
                .filter(c -> !c.getDateDebut().isAfter(today) && !c.getDateFin().isBefore(today))
                .collect(Collectors.toList());

        // Congés à venir (7 prochains jours)
        LocalDate nextWeek = today.plusDays(7);
        List<Conge> congesAVenir = congesValidesList.stream()
                .filter(c -> c.getDateDebut() != null)
                .filter(c -> c.getDateDebut().isAfter(today) && c.getDateDebut().isBefore(nextWeek))
                .collect(Collectors.toList());

        // ==================
        // STATISTIQUES
        // ==================
        int congesEnAttente = congesEnAttenteList.size();

        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        long congesValidesCeMois = congesValidesList.stream()
                .filter(c -> c.getDateDebut() != null && c.getDateDebut().isAfter(debutMois.minusDays(1)))
                .count();

        int congesRefuses = congesRefusesList.size();

        long joursCongesTotaux = congesValidesList.stream()
                .filter(c -> c.getDateDebut() != null && c.getDateDebut().getMonthValue() == LocalDate.now().getMonthValue())
                .mapToLong(c -> {
                    try {
                        return c.getNombreJours();
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();

        List<Employe> employesDept = employeRepository.findByDepartementId(departementId);
        long nombreEmployes = employesDept.size();
        double tauxAbsence = nombreEmployes > 0 ? (double) joursCongesTotaux / (nombreEmployes * 20) * 100 : 0;

        double pourcentageValidation = (congesEnAttente + congesValidesCeMois + congesRefuses) > 0
                ? (double) congesValidesCeMois / (congesEnAttente + congesValidesCeMois + congesRefuses) * 100
                : 0;

        // Ajouter au modèle
        model.addAttribute("congesEnAttenteList", congesEnAttenteList);
        model.addAttribute("congesValidesList", congesValidesList);
        model.addAttribute("congesRefusesList", congesRefusesList);
        model.addAttribute("historiqueConges", historiqueConges);
        model.addAttribute("congesAujourdhui", congesAujourdhui);
        model.addAttribute("congesAVenir", congesAVenir);

        model.addAttribute("congesEnAttente", congesEnAttente);
        model.addAttribute("congesValidesCeMois", congesValidesCeMois);
        model.addAttribute("congesRefuses", congesRefuses);
        model.addAttribute("tauxAbsence", tauxAbsence);
        model.addAttribute("joursCongesTotaux", joursCongesTotaux);
        model.addAttribute("pourcentageValidation", Math.round(pourcentageValidation));

        List<Map<String, Object>> congesCalendrier = congesValidesList.stream()
                .filter(c -> c.getDateDebut() != null && c.getDateFin() != null && c.getEmploye() != null)
                .map(c -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", c.getId());
                    m.put("dateDebut", c.getDateDebut().toString());
                    m.put("dateFin", c.getDateFin().toString());
                    m.put("typeConge", c.getType() != null ? c.getType().name() : "CONGE_PAYE");
                    m.put("statut", c.getStatut() != null ? c.getStatut().name() : "");
                    m.put("employeNom", c.getEmploye().getPrenom() + " " + c.getEmploye().getNom());
                    return m;
                })
                .toList();


        model.addAttribute("congesCalendrier", congesCalendrier);


        return "manager/conges";
    }

    @PostMapping("/conges/{id}/valider")
    public String validerConge(
            @PathVariable Long id,
            @RequestParam(required = false) String commentaire,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Employe> managerOpt = employeService.findByEmail(authentication.getName());
            if (managerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé");
                return "redirect:/manager/conges";
            }

            Employe manager = managerOpt.get();
            congeService.validerCongeManager(id, manager.getId(), true, commentaire);

            redirectAttributes.addFlashAttribute("successMessage", "Congé validé avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
        }

        return "redirect:/manager/conges";
    }

    @PostMapping("/conges/{id}/refuser")
    public String refuserConge(
            @PathVariable Long id,
            @RequestParam String motifRefus,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Employe> managerOpt = employeService.findByEmail(authentication.getName());
            if (managerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé");
                return "redirect:/manager/conges";
            }

            Employe manager = managerOpt.get();
            congeService.validerCongeManager(id, manager.getId(), false, motifRefus);

            redirectAttributes.addFlashAttribute("successMessage", "Congé refusé.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
        }

        return "redirect:/manager/conges";
    }

    // =========================
    // ✅ ROUTES MENU MANAGER
    // =========================
    @GetMapping("/employes")
    public String employes() {
        return "manager/employes";
    }

    @GetMapping("/departements")
    public String departements() {
        return "manager/departements";
    }

    private void setEmptyDashboard(Model model) {
        model.addAttribute("totalEmployes", 0);
        model.addAttribute("employesActifs", 0);
        model.addAttribute("employesInactifs", 0);

        model.addAttribute("totalConges", 0);
        model.addAttribute("congesEnAttente", 0);
        model.addAttribute("congesValides", 0);

        model.addAttribute("totalDepartements", 0);
        model.addAttribute("departementsAvecManager", 0);
        model.addAttribute("tauxOccupation", 0);

        model.addAttribute("statistiquesParRole", Map.of());
        model.addAttribute("derniersEmployes", List.of());
        model.addAttribute("departementsSansManager", List.of());
    }

    private void setEmptyCongesData(Model model) {
        model.addAttribute("congesEnAttenteList", List.of());
        model.addAttribute("congesValidesList", List.of());
        model.addAttribute("congesRefusesList", List.of());
        model.addAttribute("historiqueConges", List.of());
        model.addAttribute("congesAujourdhui", List.of());
        model.addAttribute("congesAVenir", List.of());

        model.addAttribute("congesEnAttente", 0);
        model.addAttribute("congesValidesCeMois", 0);
        model.addAttribute("congesRefuses", 0);
        model.addAttribute("tauxAbsence", 0.0);
        model.addAttribute("joursCongesTotaux", 0L);
        model.addAttribute("pourcentageValidation", 0);
        model.addAttribute("congesCalendrier", List.of());
    }
}
