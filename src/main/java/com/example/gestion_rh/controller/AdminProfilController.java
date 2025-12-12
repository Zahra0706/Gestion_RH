package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.EmployeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminProfilController {

    private final EmployeService employeService;

    @GetMapping("/profil")
    public String showProfil(Model model) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/auth/login";
            }

            String email = auth.getName();
            System.out.println("Recherche de l'utilisateur avec email: " + email);

            Employe user = employeService.getEmployeByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec email: " + email));

            System.out.println("Utilisateur trouvé: " + user.getPrenom() + " " + user.getNom());

            // Ajouter les données au modèle
            model.addAttribute("user", user);

            return "admin/profil";

        } catch (Exception e) {
            System.err.println("Erreur dans AdminProfilController: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement du profil: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }
}