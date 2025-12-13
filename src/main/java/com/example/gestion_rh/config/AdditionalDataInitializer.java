//package com.example.gestion_rh.config;
//
//import com.example.gestion_rh.entity.*;
//import com.example.gestion_rh.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Random;
//
//@Configuration
//@RequiredArgsConstructor
//public class AdditionalDataInitializer {
//
//    private final Random random = new Random();
//
//    @Bean
//    @Order(4)
//    @Transactional
//    public CommandLineRunner addTestData(
//            EmployeRepository employeRepository,
//            DepartementRepository departementRepository,
//            CongeRepository congeRepository,
//            AnnonceRepository annonceRepository,
//            ReunionRepository reunionRepository,
//            EvaluationRepository evaluationRepository,
//            SalaireRepository salaireRepository) {
//
//        return args -> {
//            System.out.println("=== Ajout de données de test pour les statistiques ===");
//
//            LocalDate today = LocalDate.now();
//            LocalDateTime now = LocalDateTime.now();
//
//            // 1. Ajouter plus d'employés dans différents départements
//            List<Departement> departements = departementRepository.findAll();
//
//            // Liste d'employés supplémentaires
//            Object[][] employesData = {
//                    // Développeurs IT
//                    {"EMP002", "MARTIN", "Pierre", "dev1@test.com", "dev123", "Développeur Java", Employe.Poste.DEVELOPPEUR, Employe.Role.EMPLOYE, "IT"},
//                    {"EMP003", "DUBOIS", "Marie", "dev2@test.com", "dev123", "Développeur Frontend", Employe.Poste.DEVELOPPEUR, Employe.Role.EMPLOYE, "IT"},
//                    {"EMP004", "ROUX", "Luc", "dev3@test.com", "dev123", "Développeur Backend", Employe.Poste.DEVELOPPEUR, Employe.Role.EMPLOYE, "IT"},
//                    {"EMP005", "LEROUX", "Sophie", "dev4@test.com", "dev123", "DevOps Engineer", Employe.Poste.DEVELOPPEUR, Employe.Role.EMPLOYE, "IT"},
//                    {"EMP006", "MOREAU", "Thomas", "dev5@test.com", "dev123", "Développeur Fullstack", Employe.Poste.DEVELOPPEUR, Employe.Role.EMPLOYE, "IT"},
//
//                    // RH
//                    {"EMP007", "PETIT", "Julie", "rh2@test.com", "rh123", "Assistante RH", Employe.Poste.RH, Employe.Role.EMPLOYE, "RH"},
//                    {"EMP008", "LAMBERT", "Alex", "rh3@test.com", "rh123", "Recruteur", Employe.Poste.RH, Employe.Role.EMPLOYE, "RH"},
//
//                    // Finance
//                    {"EMP009", "GIRARD", "Marc", "finance1@test.com", "fin123", "Comptable", Employe.Poste.ANALYSTE, Employe.Role.EMPLOYE, "FIN"},
//                    {"EMP010", "ROUSSEAU", "Claire", "finance2@test.com", "fin123", "Analyste Financier", Employe.Poste.ANALYSTE, Employe.Role.EMPLOYE, "FIN"},
//
//                    // Marketing
//                    {"EMP011", "LEFEVRE", "Anna", "marketing1@test.com", "mkt123", "Responsable Marketing", Employe.Poste.MANAGER, Employe.Role.MANAGER, "MKT"},
//                    {"EMP012", "GONZALEZ", "Carlos", "marketing2@test.com", "mkt123", "Chargé de Communication", Employe.Poste.DESIGNER, Employe.Role.EMPLOYE, "MKT"},
//
//                    // Communication
//                    {"EMP013", "BERGER", "Laura", "com1@test.com", "com123", "Community Manager", Employe.Poste.DESIGNER, Employe.Role.EMPLOYE, "COM"},
//
//                    // Logistique
//                    {"EMP014", "RENAUD", "David", "log1@test.com", "log123", "Responsable Logistique", Employe.Poste.MANAGER, Employe.Role.MANAGER, "LOG"},
//                    {"EMP015", "FOURNIER", "Elise", "log2@test.com", "log123", "Planificateur", Employe.Poste.CHEF_PROJET, Employe.Role.EMPLOYE, "LOG"},
//
//                    // Autres rôles
//                    {"EMP016", "CHEVALIER", "Paul", "admin2@test.com", "admin123", "Administrateur Système", Employe.Poste.ADMIN, Employe.Role.ADMIN, "IT"},
//                    {"EMP017", "BARBIER", "Sara", "rh4@test.com", "rh123", "Responsable Formation", Employe.Poste.RH, Employe.Role.RH, "RH"},
//                    {"EMP018", "BRUNET", "Nicolas", "manager2@test.com", "mgr123", "Manager Projet", Employe.Poste.MANAGER, Employe.Role.MANAGER, "IT"},
//                    {"EMP019", "GAUTHIER", "Isabelle", "manager3@test.com", "mgr123", "Manager Financier", Employe.Poste.MANAGER, Employe.Role.MANAGER, "FIN"},
//                    {"EMP020", "MORIN", "Alexandre", "superadmin2@test.com", "sadm123", "Super Admin", Employe.Poste.ADMIN, Employe.Role.SUPER_ADMIN, "IT"}
//            };
//
//            // Créer les employés supplémentaires
//            for (Object[] empData : employesData) {
//                String email = (String) empData[3];
//                employeRepository.findByEmail(email)
//                        .orElseGet(() -> {
//                            Departement dept = departements.stream()
//                                    .filter(d -> d.getCode().equals(empData[7]))
//                                    .findFirst()
//                                    .orElse(departements.get(0));
//
//                            Employe.Poste poste = (Employe.Poste) empData[6];
//                            Employe.Role role = (Employe.Role) empData[7];
//
//                            // Si c'est un manager, l'assigner comme manager du département
//                            if (role == Employe.Role.MANAGER && dept.getManager() == null) {
//                                dept.setManager(null); // On assignera après la création
//                            }
//
//                            Employe employe = Employe.builder()
//                                    .matricule((String) empData[0])
//                                    .nom((String) empData[1])
//                                    .prenom((String) empData[2])
//                                    .email(email)
//                                    .password(passwordEncoder.encode((String) empData[4]))
//                                    .adresse("Adresse test")
//                                    .dateEmbauche(today.minusMonths(random.nextInt(36) + 1))
//                                    .poste(poste)
//                                    .role((Employe.Role) empData[7])
//                                    .isCompteActif(random.nextBoolean() ? true : false) // 50% chance d'être inactif
//                                    .premiereConnexion(true)
//                                    .departement(dept)
//                                    .telephone("06" + (10000000 + random.nextInt(90000000)))
//                                    .build();
//
//                            Employe savedEmploye = employeRepository.save(employe);
//
//                            // Assigner comme manager si nécessaire
//                            if (role == Employe.Role.MANAGER && dept.getManager() == null) {
//                                dept.setManager(savedEmploye);
//                                departementRepository.save(dept);
//                            }
//
//                            System.out.println("Employé créé: " + email);
//                            return savedEmploye;
//                        });
//            }
//
//            // 2. Ajouter des congés pour tester les statistiques
//            List<Employe> allEmployes = employeRepository.findAll();
//
//            // Types de congés
//            Conge.TypeConge[] typesConge = Conge.TypeConge.values();
//            Conge.StatutConge[] statutsConge = {
//                    Conge.StatutConge.EN_ATTENTE_MANAGER,
//                    Conge.StatutConge.EN_ATTENTE_RH,
//                    Conge.StatutConge.VALIDE_MANAGER,
//                    Conge.StatutConge.VALIDE_RH,
//                    Conge.StatutConge.REFUSE_MANAGER,
//                    Conge.StatutConge.REFUSE_RH,
//                    Conge.StatutConge.ANNULE
//            };
//
//            for (int i = 0; i < 30; i++) {
//                Employe employe = allEmployes.get(random.nextInt(allEmployes.size()));
//
//                LocalDate startDate = today.minusDays(random.nextInt(60));
//                LocalDate endDate = startDate.plusDays(random.nextInt(14) + 1);
//
//                Conge.StatutConge statut = statutsConge[random.nextInt(statutsConge.length)];
//
//                Conge conge = Conge.builder()
//                        .employe(employe)
//                        .dateDebut(startDate)
//                        .dateFin(endDate)
//                        .typeConge(typesConge[random.nextInt(typesConge.length)])
//                        .motif("Congé " + (i + 1))
//                        .statut(statut)
//                        .build();
//
//                // Si validé, ajouter un validateur
//                if (statut == Conge.StatutConge.VALIDE_MANAGER || statut == Conge.StatutConge.REFUSE_MANAGER) {
//                    // Trouver un manager
//                    Employe manager = allEmployes.stream()
//                            .filter(e -> e.getRole() == Employe.Role.MANAGER)
//                            .findFirst()
//                            .orElse(null);
//                    if (manager != null) {
//                        conge.setManagerValidateur(manager);
//                    }
//                } else if (statut == Conge.StatutConge.VALIDE_RH || statut == Conge.StatutConge.REFUSE_RH) {
//                    // Trouver un RH
//                    Employe rh = allEmployes.stream()
//                            .filter(e -> e.getRole() == Employe.Role.RH)
//                            .findFirst()
//                            .orElse(null);
//                    if (rh != null) {
//                        conge.setRhValidateur(rh);
//                    }
//                }
//
//                congeRepository.save(conge);
//            }
//            System.out.println("30 congés créés");
//
//            // 3. Ajouter des annonces
//            Annonce.TypeAnnonce[] typesAnnonce = Annonce.TypeAnnonce.values();
//            List<String> titresAnnonces = Arrays.asList(
//                    "Nouvelle politique de télétravail",
//                    "Formation sécurité informatique",
//                    "Réunion trimestrielle",
//                    "Fermeture annuelle",
//                    "Recrutement nouveaux talents",
//                    "Mise à jour du règlement intérieur",
//                    "Événement team building",
//                    "Nouveaux équipements de bureau",
//                    "Changement des horaires",
//                    "Projet stratégique 2025"
//            );
//
//            List<String> contenusAnnonces = Arrays.asList(
//                    "Nous mettons en place une nouvelle politique de télétravail...",
//                    "Une formation sur la sécurité informatique sera organisée le mois prochain...",
//                    "La réunion trimestrielle aura lieu vendredi prochain...",
//                    "L'entreprise sera fermée du 1er au 15 août...",
//                    "Nous recrutons de nouveaux talents pour notre équipe IT...",
//                    "Le règlement intérieur a été mis à jour, merci de le consulter...",
//                    "Un événement team building est prévu pour le mois de juin...",
//                    "De nouveaux équipements de bureau sont arrivés...",
//                    "Les horaires de travail sont modifiés à partir de lundi...",
//                    "Le projet stratégique 2025 est lancé, toutes les équipes sont mobilisées..."
//            );
//
//            for (int i = 0; i < 20; i++) {
//                Employe createur = allEmployes.get(random.nextInt(allEmployes.size()));
//                Departement dept = departements.get(random.nextInt(departements.size()));
//
//                // 50% chance que l'annonce soit générale (sans département)
//                if (random.nextBoolean()) {
//                    dept = null;
//                }
//
//                Annonce annonce = Annonce.builder()
//                        .titre(titresAnnonces.get(random.nextInt(titresAnnonces.size())))
//                        .contenu(contenusAnnonces.get(random.nextInt(contenusAnnonces.size())))
//                        .type(typesAnnonce[random.nextInt(typesAnnonce.length)])
//                        .datePublication(now.minusDays(random.nextInt(90)))
//                        .createur(createur)
//                        .departement(dept)
//                        .build();
//
//                annonceRepository.save(annonce);
//            }
//            System.out.println("20 annonces créées");
//
//            // 4. Ajouter des réunions
//            for (int i = 0; i < 10; i++) {
//                Employe createur = allEmployes.stream()
//                        .filter(e -> e.getRole() == Employe.Role.MANAGER || e.getRole() == Employe.Role.RH)
//                        .findFirst()
//                        .orElse(allEmployes.get(0));
//
//                Departement dept = departements.get(random.nextInt(departements.size()));
//
//                Reunion reunion = Reunion.builder()
//                        .titre("Réunion " + (i + 1))
//                        .description("Description de la réunion " + (i + 1))
//                        .dateHeureDebut(now.plusDays(random.nextInt(30)))
//                        .dateHeureFin(now.plusDays(random.nextInt(30)).plusHours(2))
//                        .lieu("Salle " + (i + 1))
//                        .createur(createur)
//                        .departement(dept)
//                        .build();
//
//                Reunion savedReunion = reunionRepository.save(reunion);
//
//                // Ajouter des participants
//                int nbParticipants = random.nextInt(5) + 2;
//                for (int j = 0; j < nbParticipants; j++) {
//                    Employe participant = allEmployes.get(random.nextInt(allEmployes.size()));
//                    // Ajouter la relation many-to-many (selon votre implémentation)
//                    // savedReunion.addParticipant(participant);
//                }
//
//                reunionRepository.save(savedReunion);
//            }
//            System.out.println("10 réunions créées");
//
//            // 5. Ajouter des évaluations
//            Evaluation.StatutEvaluation[] statutsEvaluation = Evaluation.StatutEvaluation.values();
//
//            for (Employe employe : allEmployes) {
//                if (random.nextDouble() < 0.7) { // 70% des employés ont une évaluation
//                    Evaluation evaluation = Evaluation.builder()
//                            .employe(employe)
//                            .annee(today.getYear())
//                            .dateCreation(today.minusMonths(random.nextInt(6)))
//                            .scoreCompetences(random.nextDouble() * 5 + 5) // 5-10
//                            .scorePerformance(random.nextDouble() * 5 + 5)
//                            .scoreGlobal((random.nextDouble() * 5 + 5) * 0.7) // 3.5-7
//                            .statut(statutsEvaluation[random.nextInt(statutsEvaluation.length)])
//                            .build();
//
//                    // Ajouter un manager si disponible
//                    if (employe.getDepartement() != null && employe.getDepartement().getManager() != null) {
//                        evaluation.setManager(employe.getDepartement().getManager());
//                    }
//
//                    // Ajouter un RH si disponible
//                    Employe rh = allEmployes.stream()
//                            .filter(e -> e.getRole() == Employe.Role.RH)
//                            .findFirst()
//                            .orElse(null);
//                    if (rh != null) {
//                        evaluation.setRh(rh);
//                    }
//
//                    evaluationRepository.save(evaluation);
//                }
//            }
//            System.out.println("Évaluations créées");
//
//            // 6. Ajouter des salaires
//            for (Employe employe : allEmployes) {
//                for (int month = 1; month <= 12; month++) {
//                    // 80% chance d'avoir un salaire pour chaque mois
//                    if (random.nextDouble() < 0.8) {
//                        Salaire salaire = Salaire.builder()
//                                .employe(employe)
//                                .moisAnnee(String.format("%02d/%d", month, today.getYear()))
//                                .datePaiement(LocalDate.of(today.getYear(), month, 15))
//                                .salaireBrut(3000 + random.nextDouble() * 2000)
//                                .primes(random.nextDouble() * 500)
//                                .retenues(random.nextDouble() * 300)
//                                .build();
//
//                        // Calculer le salaire net
//                        double salaireNet = salaire.getSalaireBrut() + salaire.getPrimes() - salaire.getRetenues();
//                        salaire.setSalaireNet(salaireNet);
//
//                        salaireRepository.save(salaire);
//                    }
//                }
//            }
//            System.out.println("Salaires créés");
//
//            // 7. Ajouter des contrats
//            for (Employe employe : allEmployes) {
//                if (employe.getContrat() == null) {
//                    Contrat contrat = Contrat.builder()
//                            .employe(employe)
//                            .typeContrat("CDI")
//                            .dateDebut(employe.getDateEmbauche())
//                            .dateFin(employe.getDateEmbauche().plusYears(3))
//                            .salaireBase(3000 + random.nextDouble() * 2000)
//                            .heuresHebdomadaires(35.0)
//                            .build();
//
//                    // Sauvegarder le contrat
//                    // (dépend de votre implémentation)
//                }
//            }
//
//            System.out.println("=== Données de test ajoutées avec succès ===");
//        };
//    }
//
//    // Pour utiliser passwordEncoder
//    private final PasswordEncoder passwordEncoder;
//
//    // Vous devrez créer ces beans ou les injecter si nécessaire
//    @Bean
//    public Random random() {
//        return new Random();
//    }
//}