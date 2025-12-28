package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.*;
import com.example.gestion_rh.repository.*;
import com.example.gestion_rh.service.CongeService;
import com.example.gestion_rh.service.EmployeService;
import com.example.gestion_rh.service.EvaluationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final CongeRepository congeRepository;
    private final CongeService congeService;
    private final EmployeService employeService;
    private final AnnonceRepository annonceRepository;
    private final SalaireRepository salaireRepository;
    private final ContratRepository contratRepository;
    private final ReunionRepository reunionRepository;
    private final EvaluationRepository evaluationRepository;
    private final EvaluationService evaluationService;

    public ManagerController(EmployeRepository employeRepository,
                             DepartementRepository departementRepository,
                             CongeRepository congeRepository,
                             CongeService congeService,
                             EmployeService employeService,
                             AnnonceRepository annonceRepository,
                             SalaireRepository salaireRepository,
                             ContratRepository contratRepository,
                             ReunionRepository reunionRepository,
                             EvaluationRepository evaluationRepository,
                             EvaluationService evaluationService) {
        this.employeRepository = employeRepository;
        this.departementRepository = departementRepository;
        this.congeRepository = congeRepository;
        this.congeService = congeService;
        this.employeService = employeService;
        this.annonceRepository = annonceRepository;
        this.salaireRepository = salaireRepository;
        this.contratRepository = contratRepository;
        this.reunionRepository = reunionRepository;
        this.evaluationRepository = evaluationRepository;
        this.evaluationService = evaluationService;
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

        long congesEnAttente = congeRepository
                .countByDepartementAndStatut(deptId, Conge.StatutConge.EN_ATTENTE_MANAGER);

        long congesValides = congeRepository.countByDepartementAndStatut(
                deptId, Conge.StatutConge.VALIDE_MANAGER);

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
                    if (a.getDateEmbauche() == null && b.getDateEmbauche() == null)
                        return 0;
                    if (a.getDateEmbauche() == null)
                        return 1;
                    if (b.getDateEmbauche() == null)
                        return -1;
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
    // SALAIRES
    // =========================
    @GetMapping("/salaires")
    public String salaires(Authentication authentication, Model model) {

        String email = authentication.getName();
        Optional<Employe> managerOpt = employeRepository.findByEmail(email);

        if (managerOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Utilisateur non trouvé");
            model.addAttribute("employees", List.of());
            model.addAttribute("salairesParEmploye", Map.of());
            return "manager/salaires";
        }

        Employe manager = managerOpt.get();

        if (manager.getDepartement() == null) {
            model.addAttribute("errorMessage", "Vous n'êtes associé à aucun département");
            model.addAttribute("employees", List.of());
            model.addAttribute("salairesParEmploye", Map.of());
            return "manager/salaires";
        }

        Long deptId = manager.getDepartement().getId();
        List<Employe> employees = employeRepository.findByDepartementId(deptId);

        Map<Long, List<Salaire>> salairesParEmploye = employees.stream()
                .collect(Collectors.toMap(
                        Employe::getId,
                        e -> salaireRepository.findByEmployeId(e.getId())));

        model.addAttribute("departement", manager.getDepartement());
        model.addAttribute("employees", employees);
        model.addAttribute("salairesParEmploye", salairesParEmploye);

        return "manager/salaires";
    }

    // =========================
    // CONTRATS
    // =========================
    @GetMapping("/contrats")
    public String managerContrats(Authentication auth, Model model) {

        Employe manager = employeRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Manager introuvable"));

        if (manager.getDepartement() == null) {
            model.addAttribute("errorMessage", "Aucun département associé.");
            model.addAttribute("contrats", List.of());
            return "manager/contrats";
        }

        Long deptId = manager.getDepartement().getId();
        List<Contrat> contrats = contratRepository.findByDepartementId(deptId);

        model.addAttribute("departement", manager.getDepartement());
        model.addAttribute("contrats", contrats);

        return "manager/contrats";
    }

    // =========================
    // ✅ EVALUATIONS (SANS /manager/evaluations)
    // ✅ on garde seulement remplir/creer
    // =========================
    @GetMapping("/evaluations/{id}/remplir")
    public String remplirEvaluationForm(@PathVariable Long id,
                                        Authentication authentication,
                                        Model model) {

        Employe manager = employeRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Manager introuvable"));

        Long deptId = manager.getDepartement().getId();

        Evaluation eval = evaluationRepository.findByIdAndEmploye_Departement_Id(id, deptId)
                .orElseThrow(() -> new RuntimeException("Évaluation introuvable ou accès interdit"));

        model.addAttribute("evaluation", eval);
        model.addAttribute("departement", manager.getDepartement());

        return "manager/evaluation-remplir";
    }

    @GetMapping("/evaluations/creer")
    public String creerEvaluationForm(Authentication authentication, Model model) {

        Employe manager = employeRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Manager introuvable"));

        if (manager.getDepartement() == null) {
            model.addAttribute("errorMessage", "Aucun département associé");
            model.addAttribute("employees", List.of());
            return "evaluation-remplir";
        }

        Long deptId = manager.getDepartement().getId();

        List<Employe> employees = employeRepository.findByDepartementId(deptId).stream()
                .filter(e -> !e.getId().equals(manager.getId()))
                .toList();

        model.addAttribute("employees", employees);
        model.addAttribute("anneeActuelle", LocalDate.now().getYear());
        return "evaluation-remplir";
    }

    @PostMapping("/evaluations/{id}/remplir")
    public String remplirEvaluation(@PathVariable Long id,
                                    @RequestParam double scorePerformance,
                                    @RequestParam double scoreCompetences,
                                    @RequestParam(required = false) String commentairesManager,
                                    Authentication authentication,
                                    RedirectAttributes ra) {

        Employe manager = employeRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Manager introuvable"));

        try {
            evaluationService.remplirEvaluation(id, manager.getId(),
                    scorePerformance, scoreCompetences, commentairesManager);

            ra.addFlashAttribute("successMessage", "Évaluation enregistrée ✅");
            return "redirect:/manager/evaluations";

        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
            return "redirect:/manager/evaluations/" + id + "/remplir";
        }
    }

    @PostMapping("/evaluations/creer")
    public String creerEvaluation(@RequestParam Long employeId,
                                  @RequestParam int annee,
                                  Authentication authentication,
                                  RedirectAttributes ra) {

        Employe manager = employeRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Manager introuvable"));

        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));

        Long deptManager = manager.getDepartement() != null ? manager.getDepartement().getId() : null;
        Long deptEmploye = employe.getDepartement() != null ? employe.getDepartement().getId() : null;

        if (deptManager == null || deptEmploye == null || !deptManager.equals(deptEmploye)) {
            ra.addFlashAttribute("errorMessage", "Accès interdit : autre département");
            return "redirect:/manager/evaluations";
        }

        if (evaluationRepository.findByEmployeIdAndAnnee(employeId, annee).isPresent()) {
            ra.addFlashAttribute("errorMessage", "Une évaluation existe déjà pour cet employé et cette année.");
            return "redirect:/manager/evaluations";
        }

        Evaluation ev = Evaluation.builder()
                .annee(annee)
                .statut(Evaluation.StatutEvaluation.EN_ATTENTE)
                .dateCreation(LocalDate.now())
                .employe(employe)
                .manager(manager)
                .scorePerformance(0.0)
                .scoreCompetences(0.0)
                .scoreGlobal(0.0)
                .build();

        evaluationRepository.save(ev);

        ra.addFlashAttribute("successMessage", "Évaluation créée ✅");
        return "redirect:/manager/evaluations";
    }

    // =========================
    // UTILITAIRES
    // =========================
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
}
