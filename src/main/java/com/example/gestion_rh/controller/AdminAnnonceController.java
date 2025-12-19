package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Annonce;
import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.AnnonceService;
import com.example.gestion_rh.service.DepartementService;
import com.example.gestion_rh.service.EmployeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import jakarta.servlet.http.HttpServletRequest;

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
    public String listeAnnonces(Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Annonce.TypeAnnonce type,
            @RequestParam(required = false) Long departementId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "datePublication") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equals("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending());

        Page<Annonce> annoncesPage = annonceService.getAnnonces(keyword, type, departementId, pageable);

        model.addAttribute("annonces", annoncesPage.getContent());
        model.addAttribute("annoncesPage", annoncesPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalItems", annoncesPage.getTotalElements());
        model.addAttribute("totalPages", annoncesPage.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        // Paramètres de filtrage pour le formulaire
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedType", type != null ? type.name() : null);
        model.addAttribute("selectedDepartement",
                departementId != null ? departementService.getDepartementById(departementId).orElse(null) : null);

        // Pour préserver l'ID du département dans le select
        model.addAttribute("departementId", departementId);

        model.addAttribute("departements", departementService.getAllDepartements());

        // Statistiques globales (pour les cartes en haut) - On garde le calcul sur TOUT
        // pour l'instant comme demandé
        // Idéalement, on créerait une méthode service pour avoir des stats filtrées
        // sans tout charger, mais findAll est acceptable pour l'instant.
        List<Annonce> allAnnonces = annonceService.getAllAnnonces();
        long totalAnnonces = allAnnonces.size();
        long importantesCount = allAnnonces.stream().filter(a -> a.getType() == Annonce.TypeAnnonce.IMPORTANTE).count();
        long departementCount = allAnnonces.stream().filter(a -> a.getType() == Annonce.TypeAnnonce.DEPARTEMENT)
                .count();
        long generalesCount = allAnnonces.stream().filter(a -> a.getType() == Annonce.TypeAnnonce.GENERALE).count();
        long informationCount = allAnnonces.stream().filter(a -> a.getType() == Annonce.TypeAnnonce.INFORMATION)
                .count();

        model.addAttribute("totalAnnonces", totalAnnonces);
        model.addAttribute("importantesCount", importantesCount);
        model.addAttribute("departementCount", departementCount);
        model.addAttribute("generalesCount", generalesCount);
        model.addAttribute("informationCount", informationCount);

        model.addAttribute("user", getCurrentUser());
        model.addAttribute("requestURI", request.getRequestURI());

        return "admin/annonces/liste";
    }

    // Les anciennes méthodes séparées (/type/{type} et /departement/{id}) ne sont
    // plus nécessaires
    // car gérées par les paramètres de requête de la méthode principale.
    // On peut les supprimer ou les rediriger vers la nouvelle méthode si
    // nécessaire,
    // mais pour une refonte propre, on les supprime.
}
