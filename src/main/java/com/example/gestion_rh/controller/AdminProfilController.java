package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.service.EmployeService;
import com.example.gestion_rh.service.DepartementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminProfilController {

    private final EmployeService employeService;
    private final DepartementService departementService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profil")
    public String showProfil(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/auth/login";
            }

            String email = auth.getName();
            Employe user = employeService.getEmployeByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec email: " + email));

            model.addAttribute("user", user);
            return "admin/profil/profil";

        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement du profil: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @GetMapping("/profil/modifier")
    public String showEditProfilForm(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Employe user = employeService.getEmployeByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Liste des postes disponibles
            List<Employe.Poste> postes = Arrays.asList(Employe.Poste.values());

            // Liste des départements
            List<Departement> departements = departementService.getAllDepartements();

            model.addAttribute("user", user);
            model.addAttribute("postes", postes);
            model.addAttribute("departements", departements);

            return "admin/profil/modifier";

        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement du formulaire: " + e.getMessage());
            return "redirect:/admin/profil/profil";
        }
    }

    @PostMapping("/profil/modifier")
    public String updateProfil(@ModelAttribute Employe employe,
                               @RequestParam(required = false) Long departementId,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Employe currentUser = employeService.getEmployeByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Mettre à jour seulement les champs modifiables
            currentUser.setNom(employe.getNom());
            currentUser.setPrenom(employe.getPrenom());
            currentUser.setTelephone(employe.getTelephone());
            currentUser.setAdresse(employe.getAdresse());
            currentUser.setDateNaissance(employe.getDateNaissance());
            currentUser.setPoste(employe.getPoste());

            // Mettre à jour le département si spécifié
            if (departementId != null) {
                Departement departement = departementService.getDepartementById(departementId)
                        .orElse(null);
                currentUser.setDepartement(departement);
            }

            employeService.saveEmploye(currentUser);
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la mise à jour du profil: " + e.getMessage());
        }

        return "redirect:/admin/profil/profil";
    }

    @GetMapping("/profil/changer-mot-de-passe")
    public String showChangePasswordForm() {
        return "admin/profil/changer-mot-de-passe";
    }

    // Dans AdminProfilController.java - MODIFIEZ la méthode changePassword
    @PostMapping("/profil/changer-mot-de-passe")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Employe user = employeService.getEmployeByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Vérifier si le mot de passe actuel est correct
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mot de passe actuel incorrect");
                return "redirect:/admin/profil/changer-mot-de-passe";
            }

            // Vérifier que les nouveaux mots de passe correspondent
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas");
                return "redirect:/admin/profil/changer-mot-de-passe";
            }

            // Vérifier les exigences de sécurité
            if (newPassword.length() < 8) {
                redirectAttributes.addFlashAttribute("error",
                        "Le mot de passe doit contenir au moins 8 caractères");
                return "redirect:/admin/profil/changer-mot-de-passe";
            }

            // CORRECTION : Utiliser la méthode changerMotDePasse du service
            // ou si elle n'existe pas, sauvegarder manuellement
            try {
                // Option 1: Si la méthode existe dans le service
                employeService.changerMotDePasse(user.getId(), newPassword);
            } catch (Exception e) {
                // Option 2: Si la méthode n'existe pas, faire manuellement
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setPremiereConnexion(false);
                employeService.saveEmploye(user);
            }

            redirectAttributes.addFlashAttribute("success",
                    "Mot de passe changé avec succès. Veuillez vous reconnecter avec votre nouveau mot de passe.");

            // Déconnexion automatique après changement de mot de passe
            SecurityContextHolder.clearContext();
            return "redirect:/auth/login?passwordChanged=true";

        } catch (Exception e) {
            // Afficher le message d'erreur complet pour débogage
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du changement de mot de passe: " + e.getMessage() +
                            " | Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            return "redirect:/admin/profil/changer-mot-de-passe";
        }
    }
}