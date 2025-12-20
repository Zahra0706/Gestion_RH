    package com.example.gestion_rh.controller;

    import com.example.gestion_rh.entity.Conge;
    import com.example.gestion_rh.entity.Departement;
    import com.example.gestion_rh.entity.Employe;
    import com.example.gestion_rh.repository.*;
    import com.example.gestion_rh.service.CongeService;
    import com.example.gestion_rh.service.EmployeService;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import com.example.gestion_rh.entity.Annonce;
    import com.example.gestion_rh.entity.Salaire;
    import com.example.gestion_rh.entity.Contrat;


    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Map;
    import java.util.Optional;
    import java.util.stream.Collectors;

    @Controller
    @RequestMapping("/manager")
    public class ManagerController {

        private final EmployeRepository employeRepository;
        private final DepartementRepository departementRepository;
        private final CongeRepository congeRepository;
        private final CongeService congeService;
        private final EmployeService employeService;
        private final AnnonceRepository annonceRepository;
        private final SalaireRepository salaireRepository;
        private final ContratRepository contratRepository;




        public ManagerController(EmployeRepository employeRepository,
                                 DepartementRepository departementRepository,
                                 CongeRepository congeRepository,
                                 CongeService congeService,
                                 EmployeService employeService,
                                 AnnonceRepository annonceRepository,
                                 SalaireRepository salaireRepository,
                                 ContratRepository contratRepository) {
            this.employeRepository = employeRepository;
            this.departementRepository = departementRepository;
            this.congeRepository = congeRepository;
            this.congeService = congeService;
            this.employeService = employeService;
            this.annonceRepository = annonceRepository;
            this.salaireRepository =salaireRepository;
            this.contratRepository = contratRepository;
        }

        // =========================
        // DASHBOARD
        // =========================
        @GetMapping("/dashboard")
        public String dashboard(Model model) {

            String email = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();

            model.addAttribute("emailConnecte", email);

            Optional<Employe> managerOpt = employeRepository.findByEmail(email);

            if (managerOpt.isEmpty()) {
                setEmptyDashboard(model);
                return "manager/dashboard";
            }

            Employe manager = managerOpt.get();

            Departement dep = manager.getDepartement();
            if (dep == null || dep.getId() == null) {
                setEmptyDashboard(model);
                return "manager/dashboard";
            }

            Long deptId = dep.getId();

            long congesEnAttente = congeRepository.countByDepartementAndStatut(
                    deptId, Conge.StatutConge.EN_ATTENTE_MANAGER
            );

            long congesValides = congeRepository.countByDepartementAndStatut(
                    deptId, Conge.StatutConge.VALIDE_MANAGER
            );

            long totalConges = congesEnAttente + congesValides;

            model.addAttribute("totalConges", totalConges);
            model.addAttribute("congesEnAttente", congesEnAttente);
            model.addAttribute("congesValides", congesValides);

            List<Employe> employesDept = employeRepository.findByDepartementId(deptId);

            long totalEmployes = employesDept.size();

            long employesActifs = employesDept.stream()
                    .filter(e -> Boolean.TRUE.equals(e.getIsCompteActif()))
                    .count();

            long employesInactifs = employesDept.stream()
                    .filter(e -> !Boolean.TRUE.equals(e.getIsCompteActif()))
                    .count();

            model.addAttribute("totalEmployes", totalEmployes);
            model.addAttribute("employesActifs", employesActifs);
            model.addAttribute("employesInactifs", employesInactifs);

            Map<String, Long> statistiquesParRole = employesDept.stream()
                    .filter(e -> e.getRole() != null)
                    .collect(Collectors.groupingBy(e -> e.getRole().name(), Collectors.counting()));

            model.addAttribute("statistiquesParRole", statistiquesParRole);

            List<Employe> derniersEmployes = employesDept.stream()
                    .sorted((a, b) -> {
                        if (a.getDateEmbauche() == null && b.getDateEmbauche() == null) return 0;
                        if (a.getDateEmbauche() == null) return 1;
                        if (b.getDateEmbauche() == null) return -1;
                        return b.getDateEmbauche().compareTo(a.getDateEmbauche());
                    })
                    .limit(5)
                    .toList();

            model.addAttribute("derniersEmployes", derniersEmployes);

            List<Departement> departementsSansManager = departementRepository.findAll().stream()
                    .filter(d -> d.getManager() == null)
                    .limit(5)
                    .toList();

            long totalDepartements = departementRepository.count();
            long departementsAvecManager = totalDepartements - departementsSansManager.size();

            int tauxOccupation = totalDepartements == 0 ? 0
                    : (int) Math.round((departementsAvecManager * 100.0) / totalDepartements);

            model.addAttribute("totalDepartements", totalDepartements);
            model.addAttribute("departementsAvecManager", departementsAvecManager);
            model.addAttribute("tauxOccupation", tauxOccupation);
            model.addAttribute("departementsSansManager", departementsSansManager);

            return "manager/dashboard";
        }

        @GetMapping("/salaires")
        public String salaires(Authentication authentication, Model model) {

            String email = authentication.getName();
            Optional<Employe> managerOpt = employeRepository.findByEmail(email);

            if (managerOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Utilisateur non trouvé");
                model.addAttribute("employees", List.of());
                model.addAttribute("salairesParEmploye", Map.of());
                return "manager/salaires";
            }

            Employe manager = managerOpt.get();

            if (manager.getDepartement() == null) {
                model.addAttribute("errorMessage", "Vous n'êtes associé à aucun département");
                model.addAttribute("employees", List.of());
                model.addAttribute("salairesParEmploye", Map.of());
                return "manager/salaires";
            }

            Long deptId = manager.getDepartement().getId();

            // employés du département
            List<Employe> employees = employeRepository.findByDepartementId(deptId);

            // salaires de chaque employé (lecture seule)
            Map<Long, List<Salaire>> salairesParEmploye = employees.stream()
                    .collect(Collectors.toMap(
                            Employe::getId,
                            e -> salaireRepository.findByEmployeId(e.getId())
                    ));

            model.addAttribute("departement", manager.getDepartement());
            model.addAttribute("employees", employees);
            model.addAttribute("salairesParEmploye", salairesParEmploye);

            return "manager/salaires";
        }

        // =========================
        // ✅ GESTION DES CONGÉS
        // =========================
        @GetMapping("/conges")
        public String gestionConges(Authentication authentication, Model model) {
            Optional<Employe> managerOpt = employeService.findByEmail(authentication.getName());
            System.out.println("tis is anageopt" + managerOpt);

            if (managerOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Utilisateur non trouvé");
                setEmptyCongesData(model);
                return "manager/conges";
            }

            Employe manager = managerOpt.get();

            if (manager.getDepartement() == null) {
                model.addAttribute("errorMessage", "Vous n'êtes associé à aucun département");
                setEmptyCongesData(model);
                return "manager/conges";
            }

            Long departementId = manager.getDepartement().getId();

            // Congés en attente de validation
            List<Conge> congesEnAttenteList = congeService.getCongesEnAttenteManager(manager.getId());

            // Congés validés par le manager
            List<Conge> congesValidesList = congeService.getCongesDepartementByStatut(
                    departementId,
                    Conge.StatutConge.VALIDE_MANAGER
            );

            // Ajouter aussi les congés validés par RH
            List<Conge> congesValidesRH = congeService.getCongesDepartementByStatut(
                    departementId,
                    Conge.StatutConge.VALIDE_RH
            );
            congesValidesList.addAll(congesValidesRH);

            // Congés refusés
            List<Conge> congesRefusesList = congeService.getCongesDepartementByStatut(
                    departementId,
                    Conge.StatutConge.REFUSE_MANAGER
            );

            // Historique complet
            List<Conge> historiqueConges = congeService.getHistoriqueCongesDepartement(departementId);

            // Congés aujourd'hui
            LocalDate today = LocalDate.now();
            List<Conge> congesAujourdhui = congesValidesList.stream()
                    .filter(c -> c.getDateDebut() != null && c.getDateFin() != null)
                    .filter(c -> !c.getDateDebut().isAfter(today) && !c.getDateFin().isBefore(today))
                    .collect(Collectors.toList());

            // Congés à venir (7 prochains jours)
            LocalDate nextWeek = today.plusDays(7);
            List<Conge> congesAVenir = congesValidesList.stream()
                    .filter(c -> c.getDateDebut() != null)
                    .filter(c -> c.getDateDebut().isAfter(today) && c.getDateDebut().isBefore(nextWeek))
                    .collect(Collectors.toList());

            // ==================
            // STATISTIQUES
            // ==================
            int congesEnAttente = congesEnAttenteList.size();

            LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
            long congesValidesCeMois = congesValidesList.stream()
                    .filter(c -> c.getDateDebut() != null && c.getDateDebut().isAfter(debutMois.minusDays(1)))
                    .count();

            int congesRefuses = congesRefusesList.size();

            long joursCongesTotaux = congesValidesList.stream()
                    .filter(c -> c.getDateDebut() != null && c.getDateDebut().getMonthValue() == LocalDate.now().getMonthValue())
                    .mapToLong(c -> {
                        try {
                            return c.getNombreJours();
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .sum();

            List<Employe> employesDept = employeRepository.findByDepartementId(departementId);
            long nombreEmployes = employesDept.size();
            double tauxAbsence = nombreEmployes > 0 ? (double) joursCongesTotaux / (nombreEmployes * 20) * 100 : 0;

            double pourcentageValidation = (congesEnAttente + congesValidesCeMois + congesRefuses) > 0
                    ? (double) congesValidesCeMois / (congesEnAttente + congesValidesCeMois + congesRefuses) * 100
                    : 0;

            // Ajouter au modèle
            model.addAttribute("congesEnAttenteList", congesEnAttenteList);
            model.addAttribute("congesValidesList", congesValidesList);
            model.addAttribute("congesRefusesList", congesRefusesList);
            model.addAttribute("historiqueConges", historiqueConges);
            model.addAttribute("congesAujourdhui", congesAujourdhui);
            model.addAttribute("congesAVenir", congesAVenir);

            model.addAttribute("congesEnAttente", congesEnAttente);
            model.addAttribute("congesValidesCeMois", congesValidesCeMois);
            model.addAttribute("congesRefuses", congesRefuses);
            model.addAttribute("tauxAbsence", tauxAbsence);
            model.addAttribute("joursCongesTotaux", joursCongesTotaux);
            model.addAttribute("pourcentageValidation", Math.round(pourcentageValidation));

            List<Map<String, Object>> congesCalendrier = congesValidesList.stream()
                    .filter(c -> c.getDateDebut() != null && c.getDateFin() != null && c.getEmploye() != null)
                    .map(c -> {
                        Map<String, Object> m = new java.util.HashMap<>();
                        m.put("id", c.getId());
                        m.put("dateDebut", c.getDateDebut().toString());
                        m.put("dateFin", c.getDateFin().toString());
                        m.put("typeConge", c.getType() != null ? c.getType().name() : "CONGE_PAYE");
                        m.put("statut", c.getStatut() != null ? c.getStatut().name() : "");
                        m.put("employeNom", c.getEmploye().getPrenom() + " " + c.getEmploye().getNom());
                        return m;
                    })
                    .toList();

            model.addAttribute("congesCalendrier", congesCalendrier);

            return "manager/conges";
        }

        @PostMapping("/conges/{id}/valider")
        public String validerConge(
                @PathVariable Long id,
                @RequestParam(required = false) String commentaire,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

            System.out.println("========================================");
            System.out.println("🟢 DÉBUT VALIDATION CONGÉ");
            System.out.println("ID du congé: " + id);
            System.out.println("Commentaire: " + commentaire);
            System.out.println("Manager email: " + authentication.getName());
            System.out.println("========================================");

            try {
                Optional<Employe> managerOpt = employeService.findByEmail(authentication.getName());

                if (managerOpt.isEmpty()) {
                    System.out.println("❌ ERREUR: Manager non trouvé");
                    redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé");
                    return "redirect:/manager/conges";
                }

                Employe manager = managerOpt.get();
                System.out.println("✅ Manager trouvé: ID=" + manager.getId() + ", Nom=" + manager.getNom());

                if (manager.getDepartement() == null) {
                    System.out.println("❌ ERREUR: Manager sans département");
                    redirectAttributes.addFlashAttribute("errorMessage", "Vous n'êtes associé à aucun département");
                    return "redirect:/manager/conges";
                }

                System.out.println("✅ Département du manager: ID=" + manager.getDepartement().getId());

                // Appel du service
                Conge congeValide = congeService.validerCongeManager(id, manager.getId(), true, commentaire);

                System.out.println("✅ Congé validé avec succès! Nouveau statut: " + congeValide.getStatut());
                System.out.println("========================================");

                redirectAttributes.addFlashAttribute("successMessage",
                        "✅ Congé validé avec succès ! L'employé et le RH ont été notifiés.");

            } catch (Exception e) {
                System.out.println("❌ ERREUR lors de la validation:");
                System.out.println("Message: " + e.getMessage());
                e.printStackTrace();
                System.out.println("========================================");

                redirectAttributes.addFlashAttribute("errorMessage",
                        "❌ Erreur : " + e.getMessage());
            }

            return "redirect:/manager/conges";
        }

        @PostMapping("/conges/{id}/refuser")
        public String refuserConge(
                @PathVariable Long id,
                @RequestParam String motifRefus,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

            try {
                Optional<Employe> managerOpt = employeService.findByEmail(authentication.getName());
                if (managerOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé");
                    return "redirect:/manager/conges";
                }

                Employe manager = managerOpt.get();
                congeService.validerCongeManager(id, manager.getId(), false, motifRefus);

                redirectAttributes.addFlashAttribute("successMessage", "Congé refusé.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
            }

            return "redirect:/manager/conges";
        }
        @GetMapping("/contrats")
        public String managerContrats(Authentication auth, Model model) {

            Employe manager = employeRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Manager introuvable"));

            if (manager.getDepartement() == null) {
                model.addAttribute("errorMessage", "Aucun département associé.");
                model.addAttribute("contrats", List.of());
                return "manager/contrats";
            }

            Long deptId = manager.getDepartement().getId();

            List<Contrat> contrats = contratRepository.findByDepartementId(deptId);

            model.addAttribute("departement", manager.getDepartement());
            model.addAttribute("contrats", contrats);

            return "manager/contrats";
        }


        // =========================
    // ✅ ANNONCES (MANAGER)
    // =========================

        @GetMapping("/annonces")
        public String annonces(Authentication authentication, Model model) {

            String email = authentication.getName();
            Optional<Employe> managerOpt = employeRepository.findByEmail(email);

            if (managerOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Utilisateur non trouvé");
                model.addAttribute("annonces", List.of());
                return "manager/annonces";
            }

            Employe manager = managerOpt.get();

            if (manager.getDepartement() == null) {
                model.addAttribute("errorMessage", "Vous n'êtes associé à aucun département");
                model.addAttribute("annonces", List.of());
                return "manager/annonces";
            }

            Long deptId = manager.getDepartement().getId();

            // ✅ annonces globales (departement null) + annonces du dept
            List<Annonce> annonces = annonceRepository.findAnnoncesForDepartement(deptId);

            model.addAttribute("annonces", annonces);
            model.addAttribute("departement", manager.getDepartement());

            return "manager/annonces";
        }

        @GetMapping("/annonces/creer")
        public String creerAnnonceForm(Authentication authentication, Model model) {

            String email = authentication.getName();
            Optional<Employe> managerOpt = employeRepository.findByEmail(email);

            if (managerOpt.isEmpty() || managerOpt.get().getDepartement() == null) {
                model.addAttribute("errorMessage", "Accès impossible : manager ou département introuvable.");
                return "manager/annonce-creer";
            }

            model.addAttribute("departement", managerOpt.get().getDepartement());
            return "manager/annonce-creer";
        }

        @PostMapping("/annonces/creer")
        public String creerAnnonce(
                @RequestParam String titre,
                @RequestParam String contenu,
                @RequestParam String type,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

            try {
                String email = authentication.getName();
                Optional<Employe> managerOpt = employeRepository.findByEmail(email);

                if (managerOpt.isEmpty() || managerOpt.get().getDepartement() == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Manager ou département introuvable.");
                    return "redirect:/manager/annonces";
                }

                Employe manager = managerOpt.get();

                Annonce annonce = new Annonce();
                annonce.setTitre(titre);
                annonce.setContenu(contenu);

                // ⚠️ adapte selon ton enum exact (INFO/URGENT/EVENT etc)
                annonce.setType(Annonce.TypeAnnonce.valueOf(type));

                annonce.setCreateur(manager);
                annonce.setDepartement(manager.getDepartement());
                annonce.setDatePublication(java.time.LocalDateTime.now());

                annonceRepository.save(annonce);

                redirectAttributes.addFlashAttribute("successMessage", "Annonce publiée avec succès ✅");
                return "redirect:/manager/annonces";

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
                return "redirect:/manager/annonces/creer";
            }
        }
        @GetMapping("/annonces/{id}/modifier")
        public String afficherFormModifier(@PathVariable Long id,
                                           Authentication authentication,
                                           Model model,
                                           RedirectAttributes redirectAttributes) {
            try {
                Employe manager = employeRepository.findByEmail(authentication.getName())
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

                Annonce annonce = annonceRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Annonce introuvable"));

                // sécurité simple: seulement les annonces du département du manager
                Long deptId = manager.getDepartement().getId();
                if (annonce.getDepartement() == null || !annonce.getDepartement().getId().equals(deptId)) {
                    throw new RuntimeException("Accès interdit");
                }

                model.addAttribute("annonce", annonce);
                model.addAttribute("types", Annonce.TypeAnnonce.values());
                model.addAttribute("departement", manager.getDepartement());

                return "manager/annonce-modifier";

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
                return "redirect:/manager/annonces";
            }
        }
        @PostMapping("/annonces/{id}/modifier")
        public String modifierAnnonce(@PathVariable Long id,
                                      @RequestParam String titre,
                                      @RequestParam String contenu,
                                      @RequestParam Annonce.TypeAnnonce type,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
            try {
                Employe manager = employeRepository.findByEmail(authentication.getName())
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

                Annonce annonce = annonceRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Annonce introuvable"));

                Long deptId = manager.getDepartement().getId();
                if (annonce.getDepartement() == null || !annonce.getDepartement().getId().equals(deptId)) {
                    throw new RuntimeException("Accès interdit");
                }

                annonce.setTitre(titre);
                annonce.setContenu(contenu);
                annonce.setType(type);

                annonceRepository.save(annonce);

                redirectAttributes.addFlashAttribute("successMessage", "Annonce modifiée ✅");
                return "redirect:/manager/annonces";

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
                return "redirect:/manager/annonces";
            }
        }
        @PostMapping("/annonces/{id}/supprimer")
        public String supprimerAnnonce(@PathVariable Long id,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
            try {
                // 1) manager connecté
                Employe manager = employeRepository.findByEmail(authentication.getName())
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

                if (manager.getDepartement() == null) {
                    throw new RuntimeException("Vous n'êtes associé à aucun département");
                }

                // 2) annonce à supprimer
                Annonce annonce = annonceRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Annonce introuvable"));

                // 3) sécurité : seulement annonces du dept du manager
                Long deptId = manager.getDepartement().getId();
                if (annonce.getDepartement() == null || !annonce.getDepartement().getId().equals(deptId)) {
                    throw new RuntimeException("Accès interdit : annonce d’un autre département");
                }

                // 4) supprimer
                annonceRepository.delete(annonce);

                redirectAttributes.addFlashAttribute("successMessage", "Annonce supprimée ✅");
                return "redirect:/manager/annonces";

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
                return "redirect:/manager/annonces";
            }
        }



        // =========================
        // ✅ ROUTES MENU MANAGER
        // =========================
        @GetMapping("/employes")
        public String employes(Authentication authentication, Model model) {

            String email = authentication.getName(); // email du user connecté

            Optional<Employe> managerOpt = employeRepository.findByEmail(email);

            if (managerOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Utilisateur non trouvé");
                model.addAttribute("employees", List.of());
                return "manager/employes";
            }

            Employe manager = managerOpt.get();

            if (manager.getDepartement() == null) {
                model.addAttribute("errorMessage", "Vous n'êtes associé à aucun département");
                model.addAttribute("employees", List.of());
                return "manager/employes";
            }

            Long deptId = manager.getDepartement().getId();

            List<Employe> employees = employeRepository.findByDepartementId(deptId);

            model.addAttribute("employees", employees);
            model.addAttribute("departement", manager.getDepartement());

            return "manager/employes";
        }


        @GetMapping("/departements")
        public String departements() {
            return "manager/departements";
        }

        private void setEmptyDashboard(Model model) {
            model.addAttribute("totalEmployes", 0);
            model.addAttribute("employesActifs", 0);
            model.addAttribute("employesInactifs", 0);

            model.addAttribute("totalConges", 0);
            model.addAttribute("congesEnAttente", 0);
            model.addAttribute("congesValides", 0);

            model.addAttribute("totalDepartements", 0);
            model.addAttribute("departementsAvecManager", 0);
            model.addAttribute("tauxOccupation", 0);

            model.addAttribute("statistiquesParRole", Map.of());
            model.addAttribute("derniersEmployes", List.of());
            model.addAttribute("departementsSansManager", List.of());
        }

        private void setEmptyCongesData(Model model) {
            model.addAttribute("congesEnAttenteList", List.of());
            model.addAttribute("congesValidesList", List.of());
            model.addAttribute("congesRefusesList", List.of());
            model.addAttribute("historiqueConges", List.of());
            model.addAttribute("congesAujourdhui", List.of());
            model.addAttribute("congesAVenir", List.of());

            model.addAttribute("congesEnAttente", 0);
            model.addAttribute("congesValidesCeMois", 0);
            model.addAttribute("congesRefuses", 0);
            model.addAttribute("tauxAbsence", 0.0);
            model.addAttribute("joursCongesTotaux", 0L);
            model.addAttribute("pourcentageValidation", 0);
            model.addAttribute("congesCalendrier", List.of());
        }
    }
