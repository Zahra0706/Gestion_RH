package com.example.gestion_rh.config;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.CongeRepository;
import com.example.gestion_rh.repository.DepartementRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final com.example.gestion_rh.repository.ContratTemplateRepository contratTemplateRepository;

    @Bean
    @Order(1)
    public CommandLineRunner initDepartments(DepartementRepository departementRepository) {
        return args -> {
            // Départements par défaut
            String[][] departementsData = {
                    { "IT", "Technologie de l'Information", "Département informatique" },
                    { "RH", "Ressources Humaines", "Département des ressources humaines" },
                    { "FIN", "Finance", "Département financier" },
                    { "MKT", "Marketing", "Département marketing" },
                    { "COM", "Communication", "Département communication" },
                    { "LOG", "Logistique", "Département logistique" }
            };

            for (String[] deptData : departementsData) {
                departementRepository.findByCode(deptData[0])
                        .ifPresentOrElse(
                                dept -> {

                                    if (!dept.getNom().equals(deptData[1])) {
                                        dept.setNom(deptData[1]);
                                        dept.setDescription(deptData[2]);
                                        departementRepository.save(dept);
                                    }
                                },
                                () -> {
                                    // Créer si n'existe pas
                                    Departement dept = Departement.builder()
                                            .code(deptData[0])
                                            .nom(deptData[1])
                                            .description(deptData[2])
                                            .isActif(true)
                                            .build();
                                    departementRepository.save(dept);
                                    System.out.println("Département " + deptData[0] + " créé");
                                });
            }
        };
    }

    @Bean
    @Order(2)
    @Transactional
    public CommandLineRunner initUsers(EmployeRepository employeRepository,
            PasswordEncoder passwordEncoder,
            DepartementRepository departementRepository) {
        return args -> {
            LocalDate today = LocalDate.now();

            // 1. Super Admin
            employeRepository.findByEmail("superadmin@hr.com")
                    .ifPresentOrElse(
                            superAdmin -> {

                                if (superAdmin.getDateEmbauche() == null) {
                                    superAdmin.setDateEmbauche(today);
                                }
                                if (superAdmin.getPrenom() == null || superAdmin.getPrenom().isEmpty()) {
                                    superAdmin.setPrenom("Admin");
                                }
                                if (superAdmin.getNom() == null || superAdmin.getNom().isEmpty()
                                        || superAdmin.getNom().equals("Super")) {
                                    superAdmin.setNom("System");
                                }
                                // S'assurer que le mot de passe est correct
                                if (!superAdmin.getPassword().startsWith("$2a$")) {
                                    superAdmin.setPassword(passwordEncoder.encode("superadmin123"));
                                }
                                employeRepository.save(superAdmin);
                                System.out.println("Super Admin mis à jour");
                            },
                            () -> {
                                // Créer si n'existe pas
                                Employe superAdmin = Employe.builder()
                                        .matricule("SADM001")
                                        .nom("System")
                                        .prenom("Super Admin")
                                        .email("superadmin@hr.com")
                                        .password(passwordEncoder.encode("superadmin123"))
                                        .adresse("Siège social")
                                        .dateEmbauche(today)
                                        .poste(Employe.Poste.ADMIN)
                                        .role(Employe.Role.SUPER_ADMIN)
                                        .isCompteActif(true)
                                        .premiereConnexion(false)
                                        .build();
                                employeRepository.save(superAdmin);
                                System.out.println("Super Admin créé");
                            });

            // 2. Admin
            employeRepository.findByEmail("admin@hr.com")
                    .ifPresentOrElse(
                            admin -> {
                                if (admin.getDateEmbauche() == null) {
                                    admin.setDateEmbauche(today);
                                }
                                if (admin.getPrenom() == null || admin.getPrenom().isEmpty()) {
                                    admin.setPrenom("Admin");
                                }
                                if (admin.getNom() == null || admin.getNom().isEmpty()
                                        || admin.getNom().equals("System")) {
                                    admin.setNom("Administrator");
                                }
                                employeRepository.save(admin);
                                System.out.println("Admin mis à jour");
                            },
                            () -> {
                                Employe admin = Employe.builder()
                                        .matricule("ADM001")
                                        .nom("Administrator")
                                        .prenom("Admin")
                                        .email("admin@hr.com")
                                        .password(passwordEncoder.encode("admin123"))
                                        .adresse("Siège social")
                                        .dateEmbauche(today)
                                        .poste(Employe.Poste.ADMIN)
                                        .role(Employe.Role.ADMIN)
                                        .isCompteActif(true)
                                        .premiereConnexion(false)
                                        .build();
                                employeRepository.save(admin);
                                System.out.println("Admin créé");
                            });

            // 3. RH - avec département
            employeRepository.findByEmail("rh@test.com")
                    .ifPresentOrElse(
                            rh -> {
                                if (rh.getDateEmbauche() == null) {
                                    rh.setDateEmbauche(today);
                                }
                                if (rh.getDepartement() == null) {
                                    departementRepository.findByCode("RH")
                                            .ifPresent(rh::setDepartement);
                                }
                                employeRepository.save(rh);
                                System.out.println("RH mis à jour");
                            },
                            () -> {
                                Departement rhDept = departementRepository.findByCode("RH")
                                        .orElseGet(() -> {
                                            Departement newDept = Departement.builder()
                                                    .code("RH")
                                                    .nom("Ressources Humaines")
                                                    .isActif(true)
                                                    .build();
                                            return departementRepository.save(newDept);
                                        });

                                Employe rh = Employe.builder()
                                        .matricule("RH001")
                                        .nom("CHOKRI")
                                        .prenom("Zahra")
                                        .email("rh@test.com")
                                        .password(passwordEncoder.encode("rh123456"))
                                        .adresse("456 Avenue RH")
                                        .dateEmbauche(today)
                                        .poste(Employe.Poste.RH)
                                        .role(Employe.Role.RH)
                                        .isCompteActif(true)
                                        .premiereConnexion(true)
                                        .departement(rhDept)
                                        .build();
                                employeRepository.save(rh);
                                System.out.println("RH créé");
                            });

            // 4. Manager - avec département IT
            employeRepository.findByEmail("manager@test.com")
                    .ifPresentOrElse(
                            manager -> {
                                if (manager.getDateEmbauche() == null) {
                                    manager.setDateEmbauche(today);
                                }
                                if (manager.getDepartement() == null) {
                                    departementRepository.findByCode("IT")
                                            .ifPresent(dept -> {
                                                manager.setDepartement(dept);
                                                // Assigner comme manager du département
                                                dept.setManager(manager);
                                                departementRepository.save(dept);
                                            });
                                }
                                employeRepository.save(manager);
                                System.out.println("Manager mis à jour");
                            },
                            () -> {
                                Departement itDept = departementRepository.findByCode("IT")
                                        .orElseGet(() -> {
                                            Departement newDept = Departement.builder()
                                                    .code("IT")
                                                    .nom("Technologie de l'Information")
                                                    .isActif(true)
                                                    .build();
                                            return departementRepository.save(newDept);
                                        });

                                Employe manager = Employe.builder()
                                        .matricule("MGR001")
                                        .nom("RAZZOUK")
                                        .prenom("Majda")
                                        .email("manager@test.com")
                                        .password(passwordEncoder.encode("manager123"))
                                        .adresse("789 Boulevard Manager")
                                        .dateEmbauche(today)
                                        .poste(Employe.Poste.MANAGER)
                                        .role(Employe.Role.MANAGER)
                                        .isCompteActif(true)
                                        .premiereConnexion(true)
                                        .departement(itDept)
                                        .build();

                                employeRepository.save(manager);

                                // Assigner comme manager
                                itDept.setManager(manager);
                                departementRepository.save(itDept);

                                System.out.println("Manager créé");
                            });

            // 5. Employé
            employeRepository.findByEmail("employe@test.com")
                    .ifPresentOrElse(
                            employe -> {
                                if (employe.getDateEmbauche() == null) {
                                    employe.setDateEmbauche(today);
                                }
                                if (employe.getDepartement() == null) {
                                    departementRepository.findByCode("IT")
                                            .ifPresent(employe::setDepartement);
                                }
                                employeRepository.save(employe);
                                System.out.println("Employé mis à jour");
                            },
                            () -> {
                                Departement itDept = departementRepository.findByCode("IT")
                                        .orElseGet(() -> departementRepository.findByCode("IT")
                                                .orElseThrow(() -> new RuntimeException("Département IT non trouvé")));

                                Employe employe = Employe.builder()
                                        .matricule("EMP001")
                                        .nom("ESSAID")
                                        .prenom("Salma")
                                        .email("employe@test.com")
                                        .password(passwordEncoder.encode("employe123"))
                                        .adresse("123 Rue Test")
                                        .dateEmbauche(today)
                                        .poste(Employe.Poste.DEVELOPPEUR)
                                        .role(Employe.Role.EMPLOYE)
                                        .isCompteActif(true)
                                        .premiereConnexion(true)
                                        .departement(itDept)
                                        .build();
                                employeRepository.save(employe);
                                System.out.println("Employé créé");
                            });

            System.out.println("=== Initialisation des utilisateurs terminée ===");
        };
    }

    // Bean optionnel pour nettoyer/réparer la base
    @Bean
    @Order(3)
    @Transactional
    public CommandLineRunner repairDatabase(EmployeRepository employeRepository) {
        return args -> {
            // S'assurer que tous les employés ont une date d'embauche
            employeRepository.findAll().forEach(employe -> {
                if (employe.getDateEmbauche() == null) {
                    employe.setDateEmbauche(LocalDate.now());
                    employeRepository.save(employe);
                    System.out.println("Date d'embauche ajoutée pour: " + employe.getEmail());
                }
            });
        };
    }

    @Bean
    @Order(4)
    @Transactional
    public CommandLineRunner initSampleConges(CongeRepository congeRepository,
            EmployeRepository employeRepository) {
        return args -> {
            // Ensure we have pending requests for testing
            long pendingRequests = congeRepository.countByStatut(Conge.StatutConge.VALIDE_MANAGER)
                    + congeRepository.countByStatut(Conge.StatutConge.EN_ATTENTE_RH);

            if (pendingRequests >= 3) {
                System.out.println("Suffisance de congés en attente pour les tests (" + pendingRequests + ")");
                return;
            }

            System.out.println("Génération de nouvelles demandes de congé pour test...");

            LocalDate today = LocalDate.now();

            // Get employees to create leave requests for
            Employe employe1 = employeRepository.findByEmail("employe@test.com").orElse(null);
            Employe manager = employeRepository.findByEmail("manager@test.com").orElse(null);

            if (employe1 == null) {
                System.out.println("Pas d'employés trouvés pour créer des congés de test");
                return;
            }

            // 1. Congé en attente RH (déjà validé par manager)
            Conge conge1 = Conge.builder()
                    .employe(employe1)
                    .dateDebut(today.plusDays(10))
                    .dateFin(today.plusDays(15))
                    .typeConge(Conge.TypeConge.PAYE)
                    .motif("Vacances familiales")
                    .statut(Conge.StatutConge.VALIDE_MANAGER) // Waiting for RH
                    .managerValidateur(manager)
                    .commentaireManager("Accepté par le manager")
                    .build();
            congeRepository.save(conge1);

            // 2. Autre congé en attente RH
            Conge conge2 = Conge.builder()
                    .employe(employe1)
                    .dateDebut(today.plusDays(30))
                    .dateFin(today.plusDays(32))
                    .typeConge(Conge.TypeConge.MALADIE)
                    .motif("Rendez-vous médical")
                    .statut(Conge.StatutConge.VALIDE_MANAGER)
                    .managerValidateur(manager)
                    .build();
            congeRepository.save(conge2);

            // 3. Congé en attente RH (sans solde)
            if (manager != null) {
                Conge conge3 = Conge.builder()
                        .employe(manager)
                        .dateDebut(today.plusDays(20))
                        .dateFin(today.plusDays(25))
                        .typeConge(Conge.TypeConge.SANS_SOLDE)
                        .motif("Affaires personnelles")
                        .statut(Conge.StatutConge.EN_ATTENTE_RH) // Directly to RH for manager
                        .build();
                congeRepository.save(conge3);
            }

            // 4. Congé déjà validé par RH (pour historique)
            Conge conge4 = Conge.builder()
                    .employe(employe1)
                    .dateDebut(today.minusDays(30))
                    .dateFin(today.minusDays(25))
                    .typeConge(Conge.TypeConge.PAYE)
                    .motif("Congé précédent")
                    .statut(Conge.StatutConge.VALIDE_RH)
                    .managerValidateur(manager)
                    .commentaireRH("Validé")
                    .build();
            congeRepository.save(conge4);

            // 5. Congé refusé par RH (pour historique)
            Conge conge5 = Conge.builder()
                    .employe(employe1)
                    .dateDebut(today.minusDays(10))
                    .dateFin(today.minusDays(5))
                    .typeConge(Conge.TypeConge.PAYE)
                    .motif("Demande refusée")
                    .statut(Conge.StatutConge.REFUSE_RH)
                    .managerValidateur(manager)
                    .commentaireRH("Période non disponible")
                    .build();
            congeRepository.save(conge5);

            System.out.println("=== 5 congés de test créés ===");
        };
    }

    @Bean
    @Order(5)
    @Transactional
    public CommandLineRunner initContractTemplates() {
        return args -> {
            if (contratTemplateRepository.count() == 0) {
                // 1. Annexe Machiniste
                com.example.gestion_rh.entity.ContratTemplate t1 = new com.example.gestion_rh.entity.ContratTemplate();
                t1.setNom("Annexe - Opérateur Machiniste");
                t1.setTypeContrat(com.example.gestion_rh.entity.Contrat.TypeContrat.CDI);
                t1.setDescription("Modèle spécifique pour les opérateurs machinistes");
                t1.setContenuHtml(
                        """
                                <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                                    <h1 style="text-align: center;">ANNEXE AU CONTRAT DE TRAVAIL</h1>
                                    <h3 style="text-align: center;">Opérateur Machiniste</h3>

                                    <p>Entre les soussignés :</p>
                                    <p><strong>${entreprise.nom}</strong>, sise au ${entreprise.adresse_siege}, représentée par ${entreprise.representant_legal}.</p>
                                    <p>Et</p>
                                    <p><strong>${employe.nom_complet}</strong>, demeurant au ${employe.adresse}, né(e) le ${employe.date_naissance}.</p>

                                    <hr/>

                                    <h4>Article 1 : Objet</h4>
                                    <p>Cette annexe précise les conditions spécifiques au poste de <strong>${employe.poste}</strong>.</p>

                                    <h4>Article 2 : Unité Opérationnelle</h4>
                                    <p>Le salarié sera affecté à l'unité de production principale.</p>

                                    <h4>Article 3 : Rémunération</h4>
                                    <p>Le salaire brut mensuel est fixé à <strong>${contrat.salaire_brut} MAD</strong> pour une durée hebdomadaire de ${contrat.duree_hebdomadaire} heures.</p>

                                    <br/><br/>
                                    <table style="width: 100%;">
                                        <tr>
                                            <td style="width: 50%;">Pour l'entreprise,<br/>${entreprise.representant_legal}</td>
                                            <td style="width: 50%;">Le salarié,<br/>${employe.nom_complet}</td>
                                        </tr>
                                    </table>
                                </div>
                                """);
                contratTemplateRepository.save(t1);

                // 2. CDI Standard
                com.example.gestion_rh.entity.ContratTemplate t2 = new com.example.gestion_rh.entity.ContratTemplate();
                t2.setNom("CDI Standard");
                t2.setTypeContrat(com.example.gestion_rh.entity.Contrat.TypeContrat.CDI);
                t2.setDescription("Contrat à Durée Indéterminée classique");
                t2.setContenuHtml(
                        """
                                <div style="font-family: 'Times New Roman', serif;">
                                    <h2 style="text-align: center; text-transform: uppercase;">Contrat de Travail à Durée Indéterminée</h2>
                                    <p align="center">Réf: ${reference_contrat}</p>

                                    <p>Le présent contrat est établi entre :</p>
                                    <ul>
                                        <li>La société <strong>${entreprise.nom}</strong></li>
                                        <li>Et M./Mme <strong>${employe.nom_complet}</strong></li>
                                    </ul>

                                    <h3>Article 1 : Engagement</h3>
                                    <p>Le salarié est engagé à compter du ${contrat.date_debut} en qualité de ${employe.poste}.</p>

                                    <h3>Article 2 : Période d'essai</h3>
                                    <p>Le présent contrat ne deviendra définitif qu'à l'issue d'une période d'essai de ${contrat.periode_essai}.</p>

                                    <h3>Article 3 : Rémunération</h3>
                                    <p>En contrepartie de ses services, le salarié percevra une rémunération brute mensuelle de ${contrat.salaire_brut} MAD.</p>

                                    <p>Fait à Casablanca, le ${date_generation}.</p>
                                </div>
                                """);
                contratTemplateRepository.save(t2);

                contratTemplateRepository.save(t2);

                // 3. Contrat CDD
                com.example.gestion_rh.entity.ContratTemplate t3 = new com.example.gestion_rh.entity.ContratTemplate();
                t3.setNom("CDD Standard");
                t3.setTypeContrat(com.example.gestion_rh.entity.Contrat.TypeContrat.CDD);
                t3.setDescription("Contrat à Durée Déterminée");
                t3.setContenuHtml(
                        """
                                <div style="font-family: Arial, sans-serif;">
                                    <h2 style="text-align: center;">CONTRAT DE TRAVAIL À DURÉE DÉTERMINÉE</h2>
                                    <p align="center">Réf: ${reference_contrat}</p>

                                    <p>Entre :</p>
                                    <p><strong>${entreprise.nom}</strong>, SIRET: ${entreprise.siret}</p>
                                    <p>Et :</p>
                                    <p><strong>${employe.nom_complet}</strong>, Matricule: ${employe.matricule}</p>

                                    <h3>Article 1 : Motif</h3>
                                    <p>Ce contrat est conclu pour le motif suivant : Accroissement temporaire d'activité.</p>

                                    <h3>Article 2 : Durée</h3>
                                    <p>Le contrat débute le ${contrat.date_debut} et se termine le ${contrat.date_fin}.</p>

                                    <h3>Article 3 : Poste et Rémunération</h3>
                                    <p>Le salarié occupera le poste de ${employe.poste} pour un salaire brut de ${contrat.salaire_brut} MAD.</p>

                                    <p>Fait à Casablanca, le ${date_generation}.</p>
                                </div>
                                """);
                contratTemplateRepository.save(t3);

                // 4. Avenant
                com.example.gestion_rh.entity.ContratTemplate t4 = new com.example.gestion_rh.entity.ContratTemplate();
                t4.setNom("Avenant au contrat");
                t4.setTypeContrat(com.example.gestion_rh.entity.Contrat.TypeContrat.CDI);
                t4.setDescription("Modification des termes du contrat initial");
                t4.setContenuHtml(
                        """
                                <div style="font-family: Arial, sans-serif;">
                                    <h2 style="text-align: center;">AVENANT AU CONTRAT DE TRAVAIL</h2>

                                    <p>Le présent avenant modifie le contrat de travail initial du salarié <strong>${employe.nom_complet}</strong>.</p>

                                    <h3>Article 1 : Objet</h3>
                                    <p>Modification de la rémunération / du poste.</p>

                                    <h3>Article 2 : Nouvelles conditions</h3>
                                    <p>À compter du ${date_generation}, le salaire est porté à ${contrat.salaire_brut} MAD.</p>

                                    <p>Les autres clauses du contrat initial demeurent inchangées.</p>
                                </div>
                                """);
                contratTemplateRepository.save(t4);

                // 5. Promesse d'embauche
                com.example.gestion_rh.entity.ContratTemplate t5 = new com.example.gestion_rh.entity.ContratTemplate();
                t5.setNom("Promesse d'embauche");
                t5.setTypeContrat(com.example.gestion_rh.entity.Contrat.TypeContrat.CDI);
                t5.setDescription("Engagement d'embauche avant signature du contrat définitif");
                t5.setContenuHtml(
                        """
                                <div style="font-family: Arial, sans-serif;">
                                    <h2 style="text-align: center;">PROMESSE D'EMBAUCHE</h2>

                                    <p>Nous avons le plaisir de vous confirmer notre intention de vous engager aux conditions suivantes :</p>
                                    <ul>
                                        <li>Poste : ${employe.poste}</li>
                                        <li>Date d'entrée : ${contrat.date_debut}</li>
                                        <li>Rémunération : ${contrat.salaire_brut} MAD</li>
                                        <li>Lieu de travail : ${contrat.lieu_travail}</li>
                                    </ul>

                                    <p>Cette promesse vaut contrat de travail si acceptée avant le [Date].</p>
                                </div>
                                """);
                contratTemplateRepository.save(t5);

                // 6. Rupture Conventionnelle
                com.example.gestion_rh.entity.ContratTemplate t6 = new com.example.gestion_rh.entity.ContratTemplate();
                t6.setNom("Rupture conventionnelle");
                t6.setTypeContrat(com.example.gestion_rh.entity.Contrat.TypeContrat.CDI);
                t6.setDescription("Accord amiable de fin de contrat");
                t6.setContenuHtml("""
                        <div style="font-family: Arial, sans-serif;">
                            <h2 style="text-align: center;">CONVENTION DE RUPTURE CONVENTIONNELLE</h2>

                            <p>Entre l'employeur ${entreprise.nom} et le salarié ${employe.nom_complet}.</p>

                            <h3>Article 1 : Principe</h3>
                            <p>Les parties conviennent de rompre le contrat de travail d'un commun accord.</p>

                            <h3>Article 2 : Date de rupture</h3>
                            <p>La fin du contrat est fixée au ${contrat.date_fin}.</p>

                            <p>Fait pour valoir ce que de droit.</p>
                        </div>
                        """);
                contratTemplateRepository.save(t6);

                // 7. Contrat d'alternance
                com.example.gestion_rh.entity.ContratTemplate t7 = new com.example.gestion_rh.entity.ContratTemplate();
                t7.setNom("Contrat d'alternance");
                t7.setTypeContrat(com.example.gestion_rh.entity.Contrat.TypeContrat.CDD);
                t7.setDescription("Contrat de formation en alternance");
                t7.setContenuHtml(
                        """
                                <div style="font-family: Arial, sans-serif;">
                                    <h2 style="text-align: center;">CONTRAT DE TRAVAIL EN ALTERNANCE</h2>

                                    <p>Ce contrat a pour but d'assurer au salarié une formation professionnelle sanctionnée par un diplôme.</p>

                                    <h3>Article 1 : Tuteur</h3>
                                    <p>L'entreprise désigne M. [Nom Tuteur] comme tuteur.</p>

                                    <h3>Article 2 : Rémunération</h3>
                                    <p>${contrat.salaire_brut} MAD (prorata du SMIG selon l'âge).</p>
                                </div>
                                """);
                contratTemplateRepository.save(t7);

                System.out.println("=== Templates de contrats initialisés ===");
            }
        };
    }
}