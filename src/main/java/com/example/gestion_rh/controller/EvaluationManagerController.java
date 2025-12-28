package com.example.gestion_rh.controller;

import com.example.gestion_rh.dto.EvaluationDTO;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.Evaluation;
import com.example.gestion_rh.entity.Evaluation.StatutEvaluation;
import com.example.gestion_rh.service.EmployeService;
import com.example.gestion_rh.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/manager/evaluations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'RH', 'ADMIN')")
public class EvaluationManagerController {

    private final EvaluationService evaluationService;
    private final EmployeService employeService;

    @GetMapping
    public String mesEvaluations(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Get manager from authentication
            String email = authentication.getName();
            Employe manager = employeService.getEmployeByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Manager non trouvé"));

            List<Evaluation> evaluations = evaluationService.getEvaluationsByManager(manager.getId());

            // Separate by status
            List<Evaluation> enAttente = evaluations.stream()
                    .filter(e -> e.getStatut() == StatutEvaluation.EN_ATTENTE
                            || e.getStatut() == StatutEvaluation.EN_COURS)
                    .toList();

            List<Evaluation> completees = evaluations.stream()
                    .filter(e -> e.getStatut() == StatutEvaluation.COMPLETEE)
                    .toList();

            model.addAttribute("evaluationsEnAttente", enAttente);
            model.addAttribute("evaluationsCompletees", completees);

            return "manager/evaluations/liste";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du chargement des évaluations: " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/{id}")
    public String formulaireEvaluation(@PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            // Get manager from authentication
            String email = authentication.getName();
            Employe manager = employeService.getEmployeByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Manager non trouvé"));

            Evaluation evaluation = evaluationService.getEvaluationById(id);

            // Verify that the manager has access to this evaluation
            if (evaluation.getManager() == null || !evaluation.getManager().getId().equals(manager.getId())) {
                redirectAttributes.addFlashAttribute("error", "Vous n'avez pas accès à cette évaluation");
                return "redirect:/manager/evaluations";
            }

            // Check if evaluation is already completed
            if (evaluation.getStatut() == StatutEvaluation.COMPLETEE) {
                redirectAttributes.addFlashAttribute("warning",
                        "Cette évaluation est déjà finalisée et ne peut plus être modifiée");
                model.addAttribute("readOnly", true);
            } else {
                model.addAttribute("readOnly", false);
            }

            model.addAttribute("evaluation", evaluation);
            model.addAttribute("employe", evaluation.getEmploye());
            model.addAttribute("isTest", false);

            return "manager/evaluations/formulaire";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du chargement de l'évaluation: " + e.getMessage());
            return "redirect:/manager/evaluations";
        }
    }

    @PostMapping("/{id}")
    public String sauvegarderEvaluation(@PathVariable Long id,
            Authentication authentication,
            @ModelAttribute EvaluationDTO evaluationDTO,
            @RequestParam(required = false) boolean finaliser,
            RedirectAttributes redirectAttributes) {
        try {
            // Get manager from authentication
            String email = authentication.getName();
            Employe manager = employeService.getEmployeByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Manager non trouvé"));

            Evaluation evaluation = evaluationService.getEvaluationById(id);

            // Verify that the manager has access to this evaluation
            if (evaluation.getManager() == null || !evaluation.getManager().getId().equals(manager.getId())) {
                redirectAttributes.addFlashAttribute("error", "Vous n'avez pas le droit de modifier cette évaluation");
                return "redirect:/manager/evaluations";
            }

            // Prevent modification of completed evaluations
            if (evaluation.getStatut() == StatutEvaluation.COMPLETEE) {
                redirectAttributes.addFlashAttribute("error",
                        "Cette évaluation est déjà finalisée et ne peut plus être modifiée");
                return "redirect:/manager/evaluations/" + id;
            }

            // Set status based on finaliser flag
            if (finaliser) {
                evaluationDTO.setStatut(StatutEvaluation.COMPLETEE);
                evaluationDTO.setDateEvaluation(LocalDate.now());
            } else {
                evaluationDTO.setStatut(StatutEvaluation.EN_COURS);
            }

            evaluationService.sauvegarderEvaluation(id, evaluationDTO);

            String message = finaliser ? "Évaluation finalisée avec succès" : "Évaluation sauvegardée (brouillon)";
            redirectAttributes.addFlashAttribute("success", message);

            return "redirect:/manager/evaluations";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la sauvegarde: " + e.getMessage());
            return "redirect:/manager/evaluations/" + id;
        }
    }

    @GetMapping("/historique")
    public String historique(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Get manager from authentication
            String email = authentication.getName();
            Employe manager = employeService.getEmployeByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Manager non trouvé"));

            List<Evaluation> historique = evaluationService.getEvaluationsByManager(manager.getId()).stream()
                    .filter(e -> e.getStatut() == StatutEvaluation.COMPLETEE)
                    .toList();

            model.addAttribute("historique", historique);

            return "manager/evaluations/historique";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du chargement de l'historique: " + e.getMessage());
            return "redirect:/manager/evaluations";
        }
    }
}
