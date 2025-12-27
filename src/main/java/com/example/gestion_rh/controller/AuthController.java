package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.EmployeService;
import com.example.gestion_rh.service.impl.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomUserDetailsService customUserDetailsService;
    private final EmployeService employeService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "desactive", required = false) String desactive,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Email ou mot de passe incorrect.");
        }
        if (logout != null) {
            model.addAttribute("message", "Vous avez été déconnecté avec succès.");
        }
        if (desactive != null) {
            model.addAttribute("error", "Votre compte est désactivé. Contactez l'administrateur.");
        }
        return "auth/login";
    }

    @GetMapping("/premiere-connexion")
    public String showPremiereConnexionPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        Employe employe = customUserDetailsService.getEmployeByEmail(auth.getName());

        if (employe == null) {
            return "redirect:/auth/login";
        }

        if (!employe.getPremiereConnexion()) {
            return "redirect:/redirect-by-role";
        }

        model.addAttribute("email", auth.getName());
        return "auth/premiere-connexion";
    }

    @PostMapping("/changer-mot-de-passe")
    public String changerMotDePasse(@RequestParam String ancienMotDePasse,
                                    @RequestParam String nouveauMotDePasse,
                                    @RequestParam String confirmerMotDePasse,
                                    RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/auth/login";
            }

            String email = auth.getName();
            Employe employe = customUserDetailsService.getEmployeByEmail(email);

            if (employe == null) {
                return "redirect:/auth/login";
            }

            // Vérifier l'ancien mot de passe
            if (!passwordEncoder.matches(ancienMotDePasse, employe.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Ancien mot de passe incorrect.");
                return "redirect:/auth/premiere-connexion";
            }

            // Vérifier que les nouveaux mots de passe correspondent
            if (!nouveauMotDePasse.equals(confirmerMotDePasse)) {
                redirectAttributes.addFlashAttribute("error", "Les nouveaux mots de passe ne correspondent pas.");
                return "redirect:/auth/premiere-connexion";
            }

            // Vérifier la complexité du mot de passe
            if (nouveauMotDePasse.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "Le mot de passe doit contenir au moins 8 caractères.");
                return "redirect:/auth/premiere-connexion";
            }

            // Changer le mot de passe
            employeService.changerMotDePasse(employe.getId(), nouveauMotDePasse);

            redirectAttributes.addFlashAttribute("success", "Mot de passe changé avec succès. Vous pouvez maintenant vous connecter.");
            return "redirect:/auth/login?logout=true";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/auth/premiere-connexion";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }
}