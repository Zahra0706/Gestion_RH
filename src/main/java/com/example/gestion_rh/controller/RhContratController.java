package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.service.ContratService;
import com.example.gestion_rh.service.EmployeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rh/contrats")
@RequiredArgsConstructor
public class RhContratController {

    private final ContratService contratService;
    private final EmployeService employeService;

    @GetMapping
    public String getAllContrats(Model model) {
        model.addAttribute("contrats", contratService.getAllActiveContrats());
        model.addAttribute("stats", contratService.getContratStats());
        return "rh/contrats/liste";
    }

    @GetMapping("/nouveau")
    public String nouveauContratForm(Model model) {
        model.addAttribute("contrat", new Contrat());
        model.addAttribute("employes", employeService.getAllEmployes());
        model.addAttribute("typesContrat", Contrat.TypeContrat.values());
        return "rh/contrats/form";
    }

    @PostMapping
    public String saveContrat(@ModelAttribute Contrat contrat, RedirectAttributes redirectAttributes) {
        try {
            // Default logic before save
            if (contrat.getId() == null) {
                contrat.setStatut(Contrat.StatutContrat.ACTIF);
            }
            contratService.saveContrat(contrat);
            redirectAttributes.addFlashAttribute("success", "Contrat enregistré avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement: " + e.getMessage());
            return "redirect:/rh/contrats/nouveau";
        }
        return "redirect:/rh/contrats";
    }

    @GetMapping("/{id}")
    public String detailsContrat(@PathVariable Long id, Model model) {
        model.addAttribute("contrat", contratService.getContratById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid contract Id:" + id)));
        return "rh/contrats/details";
    }

    @PostMapping("/{id}/archiver")
    public String archiverContrat(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contratService.archiverContrat(id);
            redirectAttributes.addFlashAttribute("success", "Contrat archivé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/contrats";
    }

    @PostMapping("/{id}/valider")
    public String validerContrat(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contratService.validerContrat(id);
            redirectAttributes.addFlashAttribute("success", "Contrat validé et référencé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/contrats/" + id;
    }

    @PostMapping("/{id}/envoyer")
    public String envoyerContrat(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contratService.envoyerContrat(id);
            redirectAttributes.addFlashAttribute("success", "Contrat envoyé à l'employé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/rh/contrats/" + id;
    }
}
