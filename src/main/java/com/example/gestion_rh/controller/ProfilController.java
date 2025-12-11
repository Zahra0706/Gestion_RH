package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.EmployeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/profil")
@RequiredArgsConstructor
public class ProfilController {

    private final EmployeService employeService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String showProfil(Model model) {
        // Ajout de l'utilisateur au modèle
        addUserToModel(model);
        return "admin/profil";
    }

    @PostMapping("/modifier")
    public String modifierProfil(@ModelAttribute Employe updatedUser,
                                 RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Employe currentUser = employeService.getEmployeByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Mise à jour sélective
            currentUser.setPrenom(updatedUser.getPrenom());
            currentUser.setNom(updatedUser.getNom());
            currentUser.setEmail(updatedUser.getEmail());
            currentUser.setTelephone(updatedUser.getTelephone());
            currentUser.setAdresse(updatedUser.getAdresse());
            currentUser.setDateNaissance(updatedUser.getDateNaissance());

            employeService.saveEmploye(currentUser);
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la mise à jour: " + e.getMessage());
        }

        return "redirect:/admin/profil";
    }

    @PostMapping("/changer-mot-de-passe")
    public String changerMotDePasse(@RequestParam String currentPassword,
                                    @RequestParam String newPassword,
                                    @RequestParam String confirmPassword,
                                    RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Employe user = employeService.getEmployeByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Validation
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mot de passe actuel incorrect");
                return "redirect:/admin/profil";
            }

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error",
                        "Les nouveaux mots de passe ne correspondent pas");
                return "redirect:/admin/profil";
            }

            if (newPassword.length() < 8) {
                redirectAttributes.addFlashAttribute("error",
                        "Le mot de passe doit contenir au moins 8 caractères");
                return "redirect:/admin/profil";
            }

            // Mise à jour
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPremiereConnexion(false);
            employeService.saveEmploye(user);

            redirectAttributes.addFlashAttribute("success", "Mot de passe changé avec succès");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du changement de mot de passe: " + e.getMessage());
        }

        return "redirect:/admin/profil";
    }

    @ModelAttribute
    public void addUserToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Employe user = employeService.getEmployeByEmail(email).orElse(null);
            model.addAttribute("user", user);
        }
    }
}