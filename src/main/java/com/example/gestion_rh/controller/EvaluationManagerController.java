package com.example.gestion_rh.controller;

import com.example.gestion_rh.dto.EvaluationDTO;
import com.example.gestion_rh.entity.Evaluation;
import com.example.gestion_rh.entity.StatutEvaluation;
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

    @GetMapping
    public String mesEvaluations(Authentication authentication, Model model) {
        // TODO: Get manager ID from authentication
        Long managerId = 1L; // Simplified

        List<Evaluation> evaluations = evaluationService.getEvaluationsByManager(managerId);

        // Separate by status
        List<Evaluation> enAttente = evaluations.stream()
                .filter(e -> e.getStatut() == StatutEvaluation.EN_ATTENTE || e.getStatut() == StatutEvaluation.EN_COURS)
                .toList();

        List<Evaluation> completees = evaluations.stream()
                .filter(e -> e.getStatut() == StatutEvaluation.COMPLETEE)
                .toList();

        model.addAttribute("evaluationsEnAttente", enAttente);
        model.addAttribute("evaluationsCompletees", completees);

        return "manager/evaluations/liste";
    }

    @GetMapping("/{id}")
    public String formulaireEvaluation(@PathVariable Long id, Model model) {
        Evaluation evaluation = evaluationService.getEvaluationById(id);

        model.addAttribute("evaluation", evaluation);
        model.addAttribute("employe", evaluation.getEmploye());
        model.addAttribute("isTest", false);

        return "manager/evaluations/formulaire";
    }

    @PostMapping("/{id}")
    public String sauvegarderEvaluation(@PathVariable Long id,
            @ModelAttribute EvaluationDTO evaluationDTO,
            @RequestParam(required = false) boolean finaliser,
            RedirectAttributes redirectAttributes) {
        try {
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

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/manager/evaluations/" + id;
        }

        return "redirect:/manager/evaluations";
    }

    @GetMapping("/historique")
    public String historique(Authentication authentication, Model model) {
        // TODO: Get manager ID from authentication
        Long managerId = 1L;

        List<Evaluation> historique = evaluationService.getEvaluationsByManager(managerId).stream()
                .filter(e -> e.getStatut() == StatutEvaluation.COMPLETEE)
                .toList();

        model.addAttribute("historique", historique);

        return "manager/evaluations/historique";
    }
}
