package com.example.gestion_rh.controller;

import com.example.gestion_rh.dto.CongeRequestForm;
import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.CongeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/employe/conges")
@RequiredArgsConstructor
public class EmployeCongeController {

    private final CongeService congeService;
    private final EmployeRepository employeRepository;

    private Employe getCurrentEmploye(Authentication authentication) {
        String email = authentication.getName(); // username = email
        return employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));
    }

    // 1) Liste : Mes congés
    @GetMapping
    public String mesConges(Authentication authentication, Model model) {
        Employe employe = getCurrentEmploye(authentication);

        List<Conge> conges = congeService.getCongesByEmploye(employe.getId());
        double joursRestants = congeService.calculerJoursCongesRestants(
                employe.getId(),
                LocalDate.now().getYear()
        );

        model.addAttribute("employe", employe);
        model.addAttribute("conges", conges);
        model.addAttribute("joursRestants", joursRestants);

        return "employe/conges/index";
    }

    // 2) Formulaire : Nouvelle demande
    @GetMapping("/nouveau")
    public String nouveau(Model model, Authentication authentication) {
        Employe employe = getCurrentEmploye(authentication);

        double joursRestants = congeService.calculerJoursCongesRestants(
                employe.getId(),
                LocalDate.now().getYear()
        );

        model.addAttribute("employe", employe);
        model.addAttribute("joursRestants", joursRestants);

        if (!model.containsAttribute("congeForm")) {
            model.addAttribute("congeForm", new CongeRequestForm());
        }

        model.addAttribute("typesConge", Conge.TypeConge.values());
        return "employe/conges/nouveau";
    }

    // 3) Soumission : Demande de congé
    @PostMapping
    public String creer(@Valid @ModelAttribute("congeForm") CongeRequestForm form,
                        org.springframework.validation.BindingResult bindingResult,
                        Authentication authentication,
                        RedirectAttributes ra,
                        Model model) {

        Employe employe = getCurrentEmploye(authentication);

        if (bindingResult.hasErrors()) {
            model.addAttribute("employe", employe);
            model.addAttribute("typesConge", Conge.TypeConge.values());
            model.addAttribute("joursRestants",
                    congeService.calculerJoursCongesRestants(employe.getId(), LocalDate.now().getYear()));
            return "employe/conges/nouveau";
        }

        try {
            Conge conge = Conge.builder()
                    .dateDebut(form.getDateDebut())
                    .dateFin(form.getDateFin())
                    .typeConge(form.getTypeConge())
                    .motif(form.getMotif())
                    .employe(employe)
                    .build();

            congeService.demanderConge(conge);

            ra.addFlashAttribute("successMessage", "Votre demande de congé a été envoyée au manager.");
            return "redirect:/employe/conges";

        } catch (RuntimeException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("congeForm", form);
            return "redirect:/employe/conges/nouveau";
        }
    }

    //  4) Annuler une demande
    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable("id") Long congeId,
                          Authentication authentication,
                          RedirectAttributes ra) {

        Employe employe = getCurrentEmploye(authentication);

        try {
            congeService.annulerConge(congeId, employe.getId());
            ra.addFlashAttribute("successMessage", "Demande de congé annulée ✅");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/employe/conges";
    }
}
