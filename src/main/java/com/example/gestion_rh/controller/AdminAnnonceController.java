package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Annonce;
import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.AnnonceService;
import com.example.gestion_rh.service.DepartementService;
import com.example.gestion_rh.service.EmployeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/annonces")
@RequiredArgsConstructor
@Slf4j
public class AdminAnnonceController {

    private final AnnonceService annonceService;
    private final DepartementService departementService;
    private final EmployeService employeService;

    @GetMapping("/creer")
    public String showCreerAnnonceForm(Model model) {
        model.addAttribute("annonce", new Annonce());
        model.addAttribute("typesAnnonce", Annonce.TypeAnnonce.values());
        model.addAttribute("departements", departementService.getAllDepartements());

        // Récupérer l'utilisateur connecté via Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Employe user = employeService.getEmployeByEmail(email).orElse(null);
            model.addAttribute("user", user);
        }

        return "admin/annonces/creer";
    }

    @PostMapping("/creer")
    public String creerAnnonce(@ModelAttribute Annonce annonce,
                               @RequestParam(value = "departementId", required = false) Long departementId,
                               RedirectAttributes redirectAttributes) {
        try {
            log.info("=== DÉBUT création annonce ===");
            log.info("Titre: {}", annonce.getTitre());
            log.info("Type: {}", annonce.getType());
            log.info("Contenu: {}", annonce.getContenu());
            log.info("DepartementId: {}", departementId);

            // Récupérer l'utilisateur connecté via Spring Security
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                log.error("Aucun utilisateur authentifié!");
                redirectAttributes.addFlashAttribute("error", "Vous devez être connecté pour créer une annonce");
                return "redirect:/auth/login";
            }

            String email = auth.getName();
            log.info("Email de l'utilisateur authentifié: {}", email);

            Employe createur = employeService.getEmployeByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email: " + email));

            log.info("Créateur trouvé: {} {} (ID: {})",
                    createur.getPrenom(), createur.getNom(), createur.getId());

            // Gérer le département
            if (departementId != null && departementId > 0) {
                Departement departement = departementService.getDepartementById(departementId)
                        .orElseThrow(() -> new RuntimeException("Département non trouvé"));
                annonce.setDepartement(departement);
            } else {
                annonce.setDepartement(null);
            }

            // Créer l'annonce
            annonceService.createAnnonce(annonce, createur.getId());

            redirectAttributes.addFlashAttribute("success", "Annonce créée avec succès et notifications envoyées");
            return "redirect:/admin/annonces";

        } catch (Exception e) {
            log.error("Erreur lors de la création de l'annonce", e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la création : " + e.getMessage());
            return "redirect:/admin/annonces/creer";
        }
    }

    @GetMapping("/{id}/modifier")
    public String showModifierAnnonceForm(@PathVariable Long id, Model model) {
        Annonce annonce = annonceService.getAnnonceById(id);
        model.addAttribute("annonce", annonce);
        model.addAttribute("typesAnnonce", Annonce.TypeAnnonce.values());
        model.addAttribute("departements", departementService.getAllDepartements());

        // Récupérer l'utilisateur connecté via Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Employe user = employeService.getEmployeByEmail(email).orElse(null);
            model.addAttribute("user", user);
        }

        return "admin/annonces/modifier";
    }

    @PostMapping("/{id}/modifier")
    public String modifierAnnonce(@PathVariable Long id,
                                  @ModelAttribute Annonce annonce,
                                  @RequestParam(value = "departementId", required = false) Long departementId,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Gérer le département
            if (departementId != null && departementId > 0) {
                Departement departement = departementService.getDepartementById(departementId)
                        .orElseThrow(() -> new RuntimeException("Département non trouvé"));
                annonce.setDepartement(departement);
            } else {
                annonce.setDepartement(null);
            }

            annonceService.updateAnnonce(id, annonce);
            redirectAttributes.addFlashAttribute("success", "Annonce modifiée avec succès");
            return "redirect:/admin/annonces";

        } catch (Exception e) {
            log.error("Erreur lors de la modification de l'annonce ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la modification : " + e.getMessage());
            return "redirect:/admin/annonces/" + id + "/modifier";
        }
    }

    @PostMapping("/{id}/supprimer")
    public String supprimerAnnonce(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            annonceService.deleteAnnonce(id);
            redirectAttributes.addFlashAttribute("success", "Annonce supprimée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'annonce ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/annonces";
    }

    @ModelAttribute("user")
    public Employe getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            return employeService.getEmployeByEmail(email).orElse(null);
        }
        return null;
    }

    @GetMapping
    public String listeAnnonces(Model model) {
        List<Annonce> annonces = annonceService.getAllAnnonces();
        List<Departement> departements = departementService.getAllDepartements();

        model.addAttribute("annonces", annonces);
        model.addAttribute("departements", departements);

        // Calculer les statistiques pour éviter les erreurs Thymeleaf
        long totalAnnonces = annonces.size();
        long importantesCount = annonces.stream()
                .filter(a -> a.getType() == Annonce.TypeAnnonce.IMPORTANTE)
                .count();
        long departementCount = annonces.stream()
                .filter(a -> a.getType() == Annonce.TypeAnnonce.DEPARTEMENT)
                .count();
        long generalesCount = annonces.stream()
                .filter(a -> a.getType() == Annonce.TypeAnnonce.GENERALE)
                .count();
        long informationCount = annonces.stream()
                .filter(a -> a.getType() == Annonce.TypeAnnonce.INFORMATION)
                .count();

        model.addAttribute("totalAnnonces", totalAnnonces);
        model.addAttribute("importantesCount", importantesCount);
        model.addAttribute("departementCount", departementCount);
        model.addAttribute("generalesCount", generalesCount);
        model.addAttribute("informationCount", informationCount);

        // Récupérer l'utilisateur connecté
        model.addAttribute("user", getCurrentUser());

        return "admin/annonces/liste";
    }

    // Faites de même pour les méthodes de filtrage
    @GetMapping("/type/{type}")
    public String listeAnnoncesParType(@PathVariable String type, Model model) {
        try {
            Annonce.TypeAnnonce typeEnum = Annonce.TypeAnnonce.valueOf(type.toUpperCase());
            List<Annonce> annonces = annonceService.getAnnoncesByType(typeEnum);
            List<Departement> departements = departementService.getAllDepartements();

            model.addAttribute("annonces", annonces);
            model.addAttribute("departements", departements);
            model.addAttribute("selectedType", type);

            // Calculer les statistiques
            long totalAnnonces = annonces.size();
            model.addAttribute("totalAnnonces", totalAnnonces);
            model.addAttribute("importantesCount", 0L);
            model.addAttribute("departementCount", 0L);
            model.addAttribute("generalesCount", 0L);
            model.addAttribute("informationCount", 0L);

            model.addAttribute("user", getCurrentUser());

            return "admin/annonces/liste";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/annonces";
        }
    }

    @GetMapping("/departement/{departementId}")
    public String listeAnnoncesParDepartement(@PathVariable Long departementId, Model model) {
        try {
            List<Annonce> annonces = annonceService.getAnnoncesByDepartement(departementId);
            List<Departement> departements = departementService.getAllDepartements();

            // Trouver le département sélectionné
            Departement selectedDept = departements.stream()
                    .filter(d -> d.getId().equals(departementId))
                    .findFirst()
                    .orElse(null);

            model.addAttribute("annonces", annonces);
            model.addAttribute("departements", departements);
            model.addAttribute("selectedDepartement", selectedDept);

            // Calculer les statistiques
            long totalAnnonces = annonces.size();
            model.addAttribute("totalAnnonces", totalAnnonces);
            model.addAttribute("importantesCount", 0L);
            model.addAttribute("departementCount", 0L);
            model.addAttribute("generalesCount", 0L);
            model.addAttribute("informationCount", 0L);

            model.addAttribute("user", getCurrentUser());

            return "admin/annonces/liste";
        } catch (Exception e) {
            return "redirect:/admin/annonces";
        }
    }
}
