package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Evaluation;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.EvaluationCampagne;
import com.example.gestion_rh.entity.StatutEvaluation;
import com.example.gestion_rh.repository.EvaluationCampagneRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rh/evaluations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'ADMIN', 'SUPER_ADMIN')")
public class EvaluationCampagneController {

    private final EvaluationService evaluationService;
    private final EvaluationCampagneRepository campagneRepository;
    private final EmployeRepository employeRepository;

    @GetMapping("/campagnes")
    public String listeCampagnes(Model model) {
        List<EvaluationCampagne> campagnes = campagneRepository.findAll();
        model.addAttribute("campagnes", campagnes);
        return "rh/evaluations/campagnes/liste";
    }

    @PostMapping("/campagnes")
    public String creerCampagne(@ModelAttribute EvaluationCampagne campagne,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Get RH from authentication
            String email = authentication.getName();
            Employe rh = employeRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));

            evaluationService.creerCampagne(campagne, rh.getId());
            redirectAttributes.addFlashAttribute("success", "Campagne créée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/evaluations/campagnes";
    }

    @GetMapping("/campagnes/{id}")
    public String detailsCampagne(@PathVariable Long id, Model model) {
        EvaluationCampagne campagne = campagneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne not found"));

        List<Evaluation> evaluations = evaluationService.getEvaluationsByCampagne(id);
        Map<String, Object> stats = evaluationService.calculerStatistiquesCampagne(id);

        // Get all active employees with MANAGER role for assignment
        List<Employe> managers = employeRepository.findAll().stream()
                .filter(e -> e.getIsCompteActif() &&
                        (e.getRole() == Employe.Role.MANAGER || e.getRole() == Employe.Role.RH
                                || e.getRole() == Employe.Role.ADMIN))
                .collect(Collectors.toList());

        model.addAttribute("campagne", campagne);
        model.addAttribute("evaluations", evaluations);
        model.addAttribute("stats", stats);
        model.addAttribute("managers", managers);

        return "rh/evaluations/campagnes/details";
    }

    @PostMapping("/campagnes/{id}/generer")
    public String genererEvaluations(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            List<Evaluation> evaluations = evaluationService.genererEvaluationsPourCampagne(id);

            if (evaluations.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "Aucune évaluation générée. Vérifiez qu'il y a des employés actifs.");
            } else {
                redirectAttributes.addFlashAttribute("success",
                        evaluations.size() + " évaluation(s) générée(s) avec succès!");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log to console
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la génération: " + e.getMessage());
        }
        return "redirect:/rh/evaluations/campagnes/" + id;
    }

    @PostMapping("/campagnes/{id}/envoyer")
    public String envoyerAuxManagers(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            evaluationService.envoyerEvaluationsAuxManagers(id);
            redirectAttributes.addFlashAttribute("success", "Notifications envoyées aux managers");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/evaluations/campagnes/" + id;
    }

    @GetMapping("/campagnes/{id}/statistiques")
    @ResponseBody
    public Map<String, Object> getStatistiques(@PathVariable Long id) {
        Map<String, Object> stats = evaluationService.calculerStatistiquesCampagne(id);
        stats.put("repartition", evaluationService.getRepartitionParCategorie(id));
        return stats;
    }

    // TEST ENDPOINTS FOR RH (to test evaluation forms without manager role)
    @GetMapping("/evaluations/{id}/test-formulaire")
    public String testFormulaireEvaluation(@PathVariable Long id, Model model) {
        Evaluation evaluation = evaluationService.getEvaluationById(id);
        model.addAttribute("evaluation", evaluation);
        model.addAttribute("isTest", true);
        return "manager/evaluations/formulaire"; // Reuse manager template for testing
    }

    @PostMapping("/evaluations/{id}/test-completer")
    public String testCompleterEvaluation(@PathVariable Long id,
            @ModelAttribute Evaluation evaluationData,
            @RequestParam(required = false) String action,
            RedirectAttributes redirectAttributes) {
        Long campagneId = null;
        try {
            Evaluation evaluation = evaluationService.getEvaluationById(id);
            campagneId = evaluation.getCampagne().getId();

            // Copy scores
            evaluation.setScorePerformance(evaluationData.getScorePerformance());
            evaluation.setScoreCompetences(evaluationData.getScoreCompetences());
            evaluation.setScoreObjectifs(evaluationData.getScoreObjectifs());
            evaluation.setCommentairesManager(evaluationData.getCommentairesManager());
            evaluation.setPointsForts(evaluationData.getPointsForts());
            evaluation.setAxesAmelioration(evaluationData.getAxesAmelioration());
            evaluation.setPlanAction(evaluationData.getPlanAction());
            evaluation.setBesoinFormation(evaluationData.getBesoinFormation());

            // Calculate global score
            Double scoreGlobal = evaluationService.calculerScoreGlobal(
                    evaluation.getScorePerformance(),
                    evaluation.getScoreCompetences(),
                    evaluation.getScoreObjectifs());
            evaluation.setScoreGlobal(scoreGlobal);

            if ("finaliser".equals(action)) {
                evaluation.setStatut(StatutEvaluation.COMPLETEE);
                evaluation.setDateCompletion(LocalDate.now());
                redirectAttributes.addFlashAttribute("success", "Évaluation finalisée avec succès (TEST)!");
            } else {
                evaluation.setStatut(StatutEvaluation.EN_COURS);
                redirectAttributes.addFlashAttribute("success", "Évaluation sauvegardée en brouillon (TEST)!");
            }

            evaluationService.saveEvaluation(evaluation);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        if (campagneId != null) {
            return "redirect:/rh/evaluations/campagnes/" + campagneId;
        } else {
            return "redirect:/rh/evaluations/campagnes";
        }
    }

    @PostMapping("/evaluations/{evaluationId}/assign-manager")
    public String assignerManager(@PathVariable Long evaluationId,
            @RequestParam Long managerId,
            @RequestParam Long campagneId,
            RedirectAttributes redirectAttributes) {
        try {
            evaluationService.assignerManager(evaluationId, managerId);
            redirectAttributes.addFlashAttribute("success", "Manager assigné avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/evaluations/campagnes/" + campagneId;
    }

    @GetMapping("/api/managers/{managerId}/employes")
    @ResponseBody
    public List<Map<String, Object>> getEmployesParManager(@PathVariable Long managerId) {
        return employeRepository.findByDepartementManagerIdAndIsCompteActifTrue(managerId)
                .stream()
                .filter(e -> !e.getId().equals(managerId)) // Un manager ne peut pas s'évaluer lui-même
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", e.getId());
                    map.put("nom", e.getNom() + " " + e.getPrenom());
                    map.put("poste", e.getPoste());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/campagnes/{id}/assigner-groupe")
    public String assignerGroupe(@PathVariable Long id,
            @RequestParam Long managerId,
            @RequestParam(required = false) List<Long> employeIds,
            RedirectAttributes redirectAttributes) {
        if (employeIds == null || employeIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner au moins un employé.");
            return "redirect:/rh/evaluations/campagnes/" + id;
        }
        try {
            List<Evaluation> created = evaluationService.genererEvaluationsGroupees(id, managerId, employeIds);
            if (created.isEmpty()) {
                redirectAttributes.addFlashAttribute("warning",
                        "Aucune nouvelle évaluation créée (elles existent peut-être déjà).");
            } else {
                redirectAttributes.addFlashAttribute("success", created.size() + " évaluations assignées avec succès!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'assignation: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/rh/evaluations/campagnes/" + id;
    }

    @PostMapping("/evaluations/{id}/supprimer")
    public String supprimerEvaluation(@PathVariable Long id, @RequestParam Long campagneId,
            RedirectAttributes redirectAttributes) {
        try {
            evaluationService.supprimerEvaluation(id);
            redirectAttributes.addFlashAttribute("success", "Évaluation supprimée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/rh/evaluations/campagnes/" + campagneId;
    }
}
