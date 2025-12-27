package com.example.gestion_rh.config;

import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
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

    @Bean
    @Order(1)
    public CommandLineRunner initDepartments(DepartementRepository departementRepository) {
        return args -> {
            // Départements par défaut
            String[][] departementsData = {
                    {"IT", "Technologie de l'Information", "Département informatique"},
                    {"RH", "Ressources Humaines", "Département des ressources humaines"},
                    {"FIN", "Finance", "Département financier"},
                    {"MKT", "Marketing", "Département marketing"},
                    {"COM", "Communication", "Département communication"},
                    {"LOG", "Logistique", "Département logistique"}
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
                                }
                        );
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
                                if (superAdmin.getNom() == null || superAdmin.getNom().isEmpty() || superAdmin.getNom().equals("Super")) {
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
                            }
                    );

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
                                if (admin.getNom() == null || admin.getNom().isEmpty() || admin.getNom().equals("System")) {
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
                            }
                    );

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
                            }
                    );

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
                                        .nom("ES-SAID")
                                        .prenom("Salma")
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
                            }
                    );

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
                                        .nom("BEL GHARIB")
                                        .prenom("Oumayma")
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
                            }
                    );

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
}