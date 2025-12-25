package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.DisponibiliteEmploye;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.StatutDisponibilite;
import com.example.gestion_rh.service.DepartementService;
import com.example.gestion_rh.service.DisponibiliteService;
import com.example.gestion_rh.service.EmployeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rh/disponibilites")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'ADMIN', 'SUPER_ADMIN')")
public class DisponibiliteController {

    private final DisponibiliteService disponibiliteService;
    private final DepartementService departementService;
    private final EmployeService employeService;

    @GetMapping
    public String listeDisponibilites(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long departementId,
            Model model) {

        LocalDate selectedDate = (date != null) ? date : LocalDate.now();
        List<DisponibiliteEmploye> dispos;

        if (departementId != null) {
            dispos = disponibiliteService.getDisponibilitesParDepartement(departementId, selectedDate);
        } else {
            dispos = disponibiliteService.getDisponibilites(selectedDate);
        }

        model.addAttribute("disponibilites", dispos);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedDepartementId", departementId);
        model.addAttribute("departements", departementService.getAllDepartements());
        model.addAttribute("statuts", StatutDisponibilite.values());
        model.addAttribute("stats", disponibiliteService.getStatistiquesPresence(selectedDate));

        return "rh/disponibilites/liste";
    }

    @PostMapping("/modifier")
    @ResponseBody
    public Map<String, Object> modifierStatut(
            @RequestParam Long employeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam StatutDisponibilite statut,
            @RequestParam(required = false) String raison,
            @RequestParam(required = false) String commentaire,
            Authentication authentication) {

        // Extract RH ID from authentication
        String email = authentication.getName();
        Long rhId = employeService.getEmployeByEmail(email)
                .map(Employe::getId)
                .orElse(1L);

        try {
            DisponibiliteEmploye updated = disponibiliteService.modifierStatut(employeId, date, statut, raison,
                    commentaire, rhId);
            return Map.of("success", true, "statut", updated.getStatut().name());
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/appliquer-periode")
    public String appliquerPeriode(
            @RequestParam Long employeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam StatutDisponibilite statut,
            @RequestParam(required = false) String raison,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        Long rhId = employeService.getEmployeByEmail(email)
                .map(Employe::getId)
                .orElse(1L);

        try {
            disponibiliteService.appliquerStatutPeriode(employeId, debut, fin, statut, raison, rhId);
            redirectAttributes.addFlashAttribute("success", "Statut appliqué avec succès sur la période.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/rh/disponibilites?date=" + debut;
    }
}
