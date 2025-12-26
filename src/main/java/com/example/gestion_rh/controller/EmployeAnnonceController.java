package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.AnnonceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employe/annonces")
@RequiredArgsConstructor
public class EmployeAnnonceController {

    private final AnnonceService annonceService;
    private final EmployeRepository employeRepository;

    private Employe currentEmploye(Authentication authentication) {
        String email = authentication.getName();
        return employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé introuvable: " + email));
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        Employe employe = currentEmploye(authentication);

        Long departementId = (employe.getDepartement() != null) ? employe.getDepartement().getId() : null;

        model.addAttribute("employe", employe);
        model.addAttribute("annonces", annonceService.getAnnoncesForEmploye(employe.getId()));


        return "employe/annonces/index";
    }
}
