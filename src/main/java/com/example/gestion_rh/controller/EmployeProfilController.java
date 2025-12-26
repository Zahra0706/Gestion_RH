package com.example.gestion_rh.controller;

import com.example.gestion_rh.dto.EmployeProfileForm;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employe/profil")
@RequiredArgsConstructor
public class EmployeProfilController {

    private final EmployeRepository employeRepository;
    private final EmployeService employeService;

    private Employe currentEmploye(Authentication authentication) {
        String email = authentication.getName();
        return employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé introuvable: " + email));
    }

    @GetMapping
    public String profil(Authentication authentication, Model model) {
        Employe employe = currentEmploye(authentication);

        EmployeProfileForm form = new EmployeProfileForm();
        form.setPrenom(employe.getPrenom());
        form.setNom(employe.getNom());
        form.setTelephone(employe.getTelephone());
        form.setAdresse(employe.getAdresse());
        form.setDateNaissance(employe.getDateNaissance());

        model.addAttribute("employe", employe);
        model.addAttribute("form", form);

        return "employe/profil/index";
    }

    @PostMapping
    public String update(Authentication authentication,
                         @Valid @ModelAttribute("form") EmployeProfileForm form,
                         BindingResult bindingResult,
                         Model model) {

        Employe employe = currentEmploye(authentication);

        if (bindingResult.hasErrors()) {
            model.addAttribute("employe", employe);
            return "employe/profil/index";
        }

        employeService.updateProfil(
                employe.getId(),
                form.getNom(),
                form.getPrenom(),
                form.getTelephone(),
                form.getAdresse(),
                form.getDateNaissance(),
                employe.getPoste(),
                employe.getDepartement()
        );

        return "redirect:/employe/profil?success";
    }
}
