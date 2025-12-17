package com.example.gestion_rh.controller;

import com.example.gestion_rh.entity.Annonce;
import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.AnnonceService;
import com.example.gestion_rh.service.DepartementService;
import com.example.gestion_rh.service.EmployeService;
import com.example.gestion_rh.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EmployeService employeService;
    private final DepartementService departementService;
    private final StatistiqueService statistiqueService;
    private final AnnonceService annonceService;

    // GESTION DES DÉPARTEMENTS

    @GetMapping("/departements")
    public String listeDepartements(Model model) {
        List<Departement> departements = departementService.getAllDepartements();
        model.addAttribute("departements", departements);

        // Calculer le nombre total d'employés
        long totalEmployees = departements.stream()
                .filter(d -> d.getEmployes() != null)
                .mapToLong(d -> d.getEmployes().size())
                .sum();
        model.addAttribute("totalEmployees", totalEmployees);

        return "admin/departements/liste";
    }

    @GetMapping("/departements/creer")
    public String showCreerDepartementForm(Model model) {
        model.addAttribute("departement", new Departement());
        model.addAttribute("managers", employeService.getEmployesByRole(Employe.Role.MANAGER));
        return "admin/departements/creer";
    }

    @PostMapping("/departements/creer")
    public String creerDepartement(@ModelAttribute Departement departement,
                                   RedirectAttributes redirectAttributes) {
        try {
            departementService.saveDepartement(departement);
            redirectAttributes.addFlashAttribute("success", "Département créé avec succès");
            return "redirect:/admin/departements";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la création du département : " + e.getMessage());
            return "redirect:/admin/departements/creer";
        }
    }

    @GetMapping("/departements/{id}/modifier")
    public String showModifierDepartementForm(@PathVariable Long id, Model model) {
        Departement departement = departementService.getDepartementById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));
        model.addAttribute("departement", departement);
        model.addAttribute("managers", employeService.getEmployesByRole(Employe.Role.MANAGER));
        return "admin/departements/modifier";
    }

    @PostMapping("/departements/{id}/modifier")
    public String modifierDepartement(@PathVariable Long id,
                                      @ModelAttribute Departement departement,
                                      @RequestParam(required = false) Long managerId,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Si un manager est spécifié, le récupérer
            if (managerId != null) {
                Employe manager = employeService.getEmployeById(managerId)
                        .orElseThrow(() -> new RuntimeException("Manager non trouvé"));
                departement.setManager(manager);
            } else {
                departement.setManager(null);
            }

            departementService.updateDepartement(id, departement);
            redirectAttributes.addFlashAttribute("success", "Département modifié avec succès");
            return "redirect:/admin/departements";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la modification : " + e.getMessage());
            return "redirect:/admin/departements/" + id + "/modifier";
        }
    }

    @PostMapping("/departements/{id}/supprimer")
    public String supprimerDepartement(@PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        try {
            departementService.deleteDepartement(id);
            redirectAttributes.addFlashAttribute("success", "Département supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de supprimer le département : " + e.getMessage());
        }
        return "redirect:/admin/departements";
    }

    @PostMapping("/departements/{id}/activer")
    public String activerDepartement(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            departementService.activerDepartement(id);
            redirectAttributes.addFlashAttribute("success", "Département activé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de l'activation : " + e.getMessage());
        }
        return "redirect:/admin/departements";
    }

    @PostMapping("/departements/{id}/desactiver")
    public String desactiverDepartement(@PathVariable Long id,
                                        RedirectAttributes redirectAttributes) {
        try {
            departementService.desactiverDepartement(id);
            redirectAttributes.addFlashAttribute("success", "Département désactivé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la désactivation : " + e.getMessage());
        }
        return "redirect:/admin/departements";
    }

    @GetMapping("/departements/{id}/details")
    public String getDepartmentDetails(@PathVariable Long id, Model model) {
        System.out.println("=== DEBUT CHARGEMENT DETAILS DEPARTEMENT ID: " + id + " ===");

        try {
            // Utilisez la nouvelle méthode du service
            Departement departement = departementService.getDepartementWithDetails(id)
                    .orElse(null);

            if (departement == null) {
                model.addAttribute("error", "Département non trouvé");
                System.out.println("Département non trouvé pour ID: " + id);
            } else {
                System.out.println("=== INFORMATIONS DU DEPARTEMENT ===");
                System.out.println("Nom: " + departement.getNom());
                System.out.println("Code: " + departement.getCode());
                System.out.println("Actif: " + departement.getIsActif());
                System.out.println("Date création: " + departement.getDateCreation());

                // Vérifier le manager
                if (departement.getManager() != null) {
                    System.out.println("Manager: " + departement.getManager().getNom() + " " + departement.getManager().getPrenom());
                    System.out.println("Email manager: " + departement.getManager().getEmail());
                } else {
                    System.out.println("Manager: null");
                }

                // Calculer les statistiques
                int totalEmployes = 0;
                int employesActifs = 0;
                int employesInactifs = 0;
                int totalAnnonces = 0;

                if (departement.getEmployes() != null) {
                    totalEmployes = departement.getEmployes().size();
                    System.out.println("Nombre d'employés: " + totalEmployes);

                    // Compter les employés actifs et inactifs
                    for (Employe employe : departement.getEmployes()) {
                        if (employe.getIsCompteActif() != null && employe.getIsCompteActif() ) {
                            employesActifs++;
                        } else {
                            employesInactifs++;
                        }
                    }
                    System.out.println("Employés actifs: " + employesActifs);
                    System.out.println("Employés inactifs: " + employesInactifs);
                } else {
                    System.out.println("Liste d'employés: null");
                }

                // Vérifier les annonces
                if (departement.getAnnonces() != null) {
                    totalAnnonces = departement.getAnnonces().size();
                    System.out.println("Nombre d'annonces: " + totalAnnonces);
                } else {
                    System.out.println("Liste d'annonces: null");
                }

                // Ajouter les statistiques au modèle
                model.addAttribute("totalEmployes", totalEmployes);
                model.addAttribute("employesActifs", employesActifs);
                model.addAttribute("employesInactifs", employesInactifs);
                model.addAttribute("totalAnnonces", totalAnnonces);

                System.out.println("=== FIN INFORMATIONS ===");
            }

            model.addAttribute("departement", departement);

        } catch (Exception e) {
            System.err.println("ERREUR dans getDepartmentDetails: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement des détails: " + e.getMessage());
        }

        System.out.println("=== FIN CHARGEMENT DETAILS ===");
        return "admin/departements/details";
    }

    @GetMapping("/departements/actifs")
    public String listeDepartementsActifs(Model model) {
        List<Departement> departements = departementService.getDepartementsActifs();
        model.addAttribute("departements", departements);
        model.addAttribute("titre", "Départements Actifs");

        long totalEmployees = departements.stream()
                .filter(d -> d.getEmployes() != null)
                .mapToLong(d -> d.getEmployes().size())
                .sum();
        model.addAttribute("totalEmployees", totalEmployees);

        return "admin/departements/liste";
    }

    @GetMapping("/departements/inactifs")
    public String listeDepartementsInactifs(Model model) {
        List<Departement> departements = departementService.getDepartementsInactifs();
        model.addAttribute("departements", departements);
        model.addAttribute("titre", "Départements Inactifs");

        long totalEmployees = departements.stream()
                .filter(d -> d.getEmployes() != null)
                .mapToLong(d -> d.getEmployes().size())
                .sum();
        model.addAttribute("totalEmployees", totalEmployees);

        return "admin/departements/liste";
    }

    @GetMapping("/departements/sans-manager")
    public String listeDepartementsSansManager(Model model) {
        List<Departement> departements = departementService.getDepartementsSansManager();
        model.addAttribute("departements", departements);
        model.addAttribute("titre", "Départements Sans Manager");

        long totalEmployees = departements.stream()
                .filter(d -> d.getEmployes() != null)
                .mapToLong(d -> d.getEmployes().size())
                .sum();
        model.addAttribute("totalEmployees", totalEmployees);

        return "admin/departements/liste";
    }

    // Gestion des employés

    @PostMapping("/employes/creer")
    public String creerEmploye(@ModelAttribute Employe employe,
                               @RequestParam(required = false) String password,
                               RedirectAttributes redirectAttributes) {
        try {
            // Générer un matricule automatique
            if (employe.getMatricule() == null || employe.getMatricule().isEmpty()) {
                String prefix = switch (employe.getRole()) {
                    case ADMIN, SUPER_ADMIN -> "ADM";
                    case RH -> "RH";
                    case MANAGER -> "MGR";
                    default -> "EMP";
                };
                employe.setMatricule(prefix + String.format("%04d",
                        employeService.getAllEmployes().size() + 1));
            }

            // Définir la date d'embauche si non spécifiée
            if (employe.getDateEmbauche() == null) {
                employe.setDateEmbauche(LocalDate.now());
            }

            employeService.creerEmploye(employe, password);
            redirectAttributes.addFlashAttribute("success",
                    "Employé créé avec succès. Les identifiants ont été envoyés par email.");
            return "redirect:/admin/employes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la création de l'employé : " + e.getMessage());
            return "redirect:/admin/employes/creer";
        }
    }

    @GetMapping("/employes/{id}/modifier")
    public String showModifierEmployeForm(@PathVariable Long id, Model model) {
        Employe employe = employeService.getEmployeById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
        model.addAttribute("employe", employe);
        model.addAttribute("departements", departementService.getAllDepartements());
        model.addAttribute("postes", Employe.Poste.values());
        model.addAttribute("roles", Employe.Role.values());
        return "admin/employes/modifier";
    }

    @PostMapping("/employes/{id}/modifier")
    public String modifierEmploye(@PathVariable Long id,
                                  @ModelAttribute Employe employe,
                                  RedirectAttributes redirectAttributes) {
        try {
            employeService.updateEmploye(id, employe);
            redirectAttributes.addFlashAttribute("success", "Employé modifié avec succès");
            return "redirect:/admin/employes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la modification : " + e.getMessage());
            return "redirect:/admin/employes/" + id + "/modifier";
        }
    }

    @PostMapping("/employes/{id}/desactiver")
    public String desactiverCompte(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            employeService.desactiverCompte(id);
            redirectAttributes.addFlashAttribute("success", "Compte désactivé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la désactivation : " + e.getMessage());
        }
        return "redirect:/admin/employes";
    }

    @PostMapping("/employes/{id}/activer")
    public String activerCompte(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            employeService.activerCompte(id);
            redirectAttributes.addFlashAttribute("success", "Compte activé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de l'activation : " + e.getMessage());
        }
        return "redirect:/admin/employes";
    }

    @PostMapping("/employes/{id}/reinitialiser-mot-de-passe")
    public String reinitialiserMotDePasse(@PathVariable Long id,
                                          RedirectAttributes redirectAttributes) {
        try {
            employeService.reinitialiserMotDePasse(id);
            redirectAttributes.addFlashAttribute("success",
                    "Mot de passe réinitialisé et envoyé par email");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la réinitialisation : " + e.getMessage());
        }
        return "redirect:/admin/employes";
    }


    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request,
                            Authentication authentication) {
        // Récupérer les statistiques
        model.addAllAttributes(statistiqueService.getStatistiquesGenerales());
        model.addAttribute("statistiquesParRole", statistiqueService.getStatistiquesParRole());

        // Derniers employés ajoutés
        List<Employe> derniersEmployes = employeService.getAllEmployes().stream()
                .sorted((e1, e2) -> e2.getDateEmbauche().compareTo(e1.getDateEmbauche()))
                .limit(5)
                .toList();

        // Formatage des dates pour les derniers employés
        List<Map<String, Object>> derniersEmployesFormatted = derniersEmployes.stream()
                .map(employe -> {
                    Map<String, Object> empMap = new HashMap<>();
                    empMap.put("id", employe.getId());
                    empMap.put("nom", employe.getNom());
                    empMap.put("prenom", employe.getPrenom());
                    empMap.put("email", employe.getEmail());
                    empMap.put("poste", employe.getPoste());

                    // Formatter la date d'embauche
                    String dateFormatted = "";
                    if (employe.getDateEmbauche() != null) {
                        dateFormatted = employe.getDateEmbauche().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        );
                    }
                    empMap.put("dateEmbaucheFormatted", dateFormatted);

                    return empMap;
                })
                .collect(Collectors.toList());

        model.addAttribute("derniersEmployesFormatted", derniersEmployesFormatted);

        // Départements sans manager
        List<Departement> departementsSansManager = departementService.getDepartementsSansManager();
        model.addAttribute("departementsSansManager", departementsSansManager);

        // Ajouter requestURI au modèle pour le menu
        model.addAttribute("requestURI", request.getRequestURI());

        // Ajouter l'utilisateur connecté au modèle
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Employe user = employeService.getEmployeByEmail(email)
                    .orElse(null);
            model.addAttribute("user", user);
        }

        return "admin/dashboard";
    }

    @GetMapping("/employes")
    public String listeEmployes(Model model) {
        model.addAttribute("employes", employeService.getAllEmployes());
        model.addAttribute("departements", departementService.getAllDepartements());
        model.addAttribute("roles", Employe.Role.values());
        return "admin/employes/liste";
    }

    @GetMapping("/employes/creer")
    public String showCreerEmployeForm(Model model) {
        model.addAttribute("employe", new Employe());
        model.addAttribute("departements", departementService.getAllDepartements());
        model.addAttribute("postes", Employe.Poste.values());
        model.addAttribute("roles", Employe.Role.values());
        return "admin/employes/creer";
    }

    @GetMapping("/employes/actifs")
    public String listeEmployesActifs(Model model) {
        model.addAttribute("employes", employeService.getEmployesActifs());
        model.addAttribute("titre", "Employés Actifs");
        model.addAttribute("departements", departementService.getAllDepartements());
        model.addAttribute("roles", Employe.Role.values());
        return "admin/employes/liste";
    }

    @GetMapping("/employes/inactifs")
    public String listeEmployesInactifs(Model model) {
        model.addAttribute("employes", employeService.getEmployesInactifs());
        model.addAttribute("titre", "Employés Inactifs");
        model.addAttribute("departements", departementService.getAllDepartements());
        model.addAttribute("roles", Employe.Role.values());
        return "admin/employes/liste";
    }

    @GetMapping("/statistiques")
    public String statistiques(Model model) {
        // Récupérer toutes les statistiques
        Map<String, Object> stats = statistiqueService.getStatistiquesGenerales();
        model.addAllAttributes(stats);

        // Ajouter les statistiques par rôle
        model.addAttribute("statistiquesParRole", statistiqueService.getStatistiquesParRole());

        // Récupérer les départements pour le tableau détaillé
        List<Departement> departements = departementService.getAllDepartements();
        model.addAttribute("departements", departements);

        // Calculer les statistiques d'annonces
        List<Annonce> annonces = annonceService.getAllAnnonces();
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

        // NOUVEAU : Données d'évolution des congés par mois
        Map<String, Object> congesEvolution = statistiqueService.getEvolutionCongesParMois(2024); // Année en cours
        model.addAttribute("congesEvolutionLabels", congesEvolution.get("labels"));
        model.addAttribute("congesEvolutionData", congesEvolution.get("data"));

        // Ajouter l'utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Employe user = employeService.getEmployeByEmail(email).orElse(null);
            model.addAttribute("user", user);
        }

        return "admin/statistiques";
    }
    @GetMapping("/statistiques/conges/evolution")
    @ResponseBody
    public Map<String, Object> getEvolutionCongesData(@RequestParam(defaultValue = "2024") int annee) {
        return statistiqueService.getEvolutionCongesParMois(annee);
    }
}