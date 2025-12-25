package com.example.gestion_rh.controller;

import com.example.gestion_rh.dto.EvaluationDTO;
import com.example.gestion_rh.entity.Evaluation;
import com.example.gestion_rh.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rh/evaluations/individuelles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'ADMIN', 'SUPER_ADMIN')")
public class EvaluationRHController {

    private final EvaluationService evaluationService;

    @GetMapping("/employe/{employeId}")
    public String historiqueEmploye(@PathVariable Long employeId, Model model) {
        List<Evaluation> historique = evaluationService.getEvaluationsByEmploye(employeId);

        model.addAttribute("historique", historique);
        model.addAttribute("employeId", employeId);

        return "rh/evaluations/individuelles/historique";
    }

    @PostMapping("/{id}/commenter")
    public String ajouterCommentaireRH(@PathVariable Long id,
            @RequestParam String commentairesRH,
            RedirectAttributes redirectAttributes) {
        try {
            Evaluation evaluation = evaluationService.getEvaluationById(id);

            EvaluationDTO dto = new EvaluationDTO();
            dto.setCommentairesRH(commentairesRH);
            dto.setStatut(evaluation.getStatut());

            evaluationService.sauvegarderEvaluation(id, dto);

            redirectAttributes.addFlashAttribute("success", "Commentaire RH ajouté avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/rh/evaluations/individuelles/employe/" +
                evaluationService.getEvaluationById(id).getEmploye().getId();
    }

    @GetMapping("/rapport/{campagneId}")
    public String rapportComplet(@PathVariable Long campagneId, Model model) {
        List<Evaluation> evaluations = evaluationService.getEvaluationsByCampagne(campagneId);
        Map<String, Object> stats = evaluationService.calculerStatistiquesCampagne(campagneId);
        Map<String, Long> repartition = evaluationService.getRepartitionParCategorie(campagneId);

        model.addAttribute("evaluations", evaluations);
        model.addAttribute("stats", stats);
        model.addAttribute("repartition", repartition);
        model.addAttribute("campagneId", campagneId);

        return "rh/evaluations/individuelles/rapport";
    }
}
