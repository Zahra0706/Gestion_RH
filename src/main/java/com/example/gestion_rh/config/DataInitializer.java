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

@Configuration
public class DataInitializer {

    @Bean
    @Order(1)
    public CommandLineRunner initDepartments(DepartementRepository departementRepository) {
        return args -> {
            if (departementRepository.count() == 0) {
                // Créer des départements par défaut
                Departement it = Departement.builder()
                        .code("IT")
                        .nom("Technologie de l'Information")
                        .description("Département informatique")
                        .isActif(true)
                        .build();

                Departement rh = Departement.builder()
                        .code("RH")
                        .nom("Ressources Humaines")
                        .description("Département des ressources humaines")
                        .isActif(true)
                        .build();

                Departement fin = Departement.builder()
                        .code("FIN")
                        .nom("Finance")
                        .description("Département financier")
                        .isActif(true)
                        .build();

                Departement mkt = Departement.builder()
                        .code("MKT")
                        .nom("Marketing")
                        .description("Département marketing")
                        .isActif(true)
                        .build();

                departementRepository.save(it);
                departementRepository.save(rh);
                departementRepository.save(fin);
                departementRepository.save(mkt);

                System.out.println("Départements créés avec succès");
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
            // Super Admin
            if (employeRepository.findByEmail("superadmin@hr.com").isEmpty()) {
                Employe superAdmin = Employe.builder()
                        .matricule("SADM001")
                        .nom("Super")
                        .prenom("Admin")
                        .email("superadmin@hr.com")
                        .password(passwordEncoder.encode("superadmin123"))
                        .adresse("Siège social")
                        .dateEmbauche(java.time.LocalDate.now())
                        .poste(Employe.Poste.ADMIN)
                        .role(Employe.Role.SUPER_ADMIN)
                        .isCompteActif(true)
                        .premiereConnexion(false)
                        .build();

                employeRepository.save(superAdmin);
                System.out.println("Super Admin créé avec succès");
            }

            // Admin
            if (employeRepository.findByEmail("admin@hr.com").isEmpty()) {
                Employe admin = Employe.builder()
                        .matricule("ADM001")
                        .nom("Admin")
                        .prenom("System")
                        .email("admin@hr.com")
                        .password(passwordEncoder.encode("admin123"))
                        .adresse("Siège social")
                        .dateEmbauche(java.time.LocalDate.now())
                        .poste(Employe.Poste.ADMIN)
                        .role(Employe.Role.ADMIN)
                        .isCompteActif(true)
                        .premiereConnexion(false)
                        .build();

                employeRepository.save(admin);
                System.out.println("Admin créé avec succès");
            }

            // RH
            if (employeRepository.findByEmail("rh@test.com").isEmpty()) {
                Departement rhDepartement = departementRepository.findByCode("RH")
                        .orElseThrow(() -> new RuntimeException("Département RH non trouvé"));

                Employe rh = Employe.builder()
                        .matricule("RH001")
                        .nom("Martin")
                        .prenom("Sophie")
                        .email("rh@test.com")
                        .password(passwordEncoder.encode("rh123456"))
                        .adresse("456 Avenue RH")
                        .dateEmbauche(java.time.LocalDate.now())
                        .poste(Employe.Poste.RH)
                        .role(Employe.Role.RH)
                        .isCompteActif(true)
                        .premiereConnexion(true)
                        .departement(rhDepartement)
                        .build();

                employeRepository.save(rh);
                System.out.println("RH créé avec succès");
            }

            // Manager
            if (employeRepository.findByEmail("manager@test.com").isEmpty()) {
                Departement itDepartement = departementRepository.findByCode("IT")
                        .orElseThrow(() -> new RuntimeException("Département IT non trouvé"));

                Employe manager = Employe.builder()
                        .matricule("MGR001")
                        .nom("Leroy")
                        .prenom("Pierre")
                        .email("manager@test.com")
                        .password(passwordEncoder.encode("manager123"))
                        .adresse("789 Boulevard Manager")
                        .dateEmbauche(java.time.LocalDate.now())
                        .poste(Employe.Poste.MANAGER)
                        .role(Employe.Role.MANAGER)
                        .isCompteActif(true)
                        .premiereConnexion(true)
                        .departement(itDepartement)
                        .build();

                employeRepository.save(manager);

                // Assigner le manager au département
                itDepartement.setManager(manager);
                departementRepository.save(itDepartement);

                System.out.println("Manager créé avec succès");
            }

            // Employé
            if (employeRepository.findByEmail("employe@test.com").isEmpty()) {
                Departement itDepartement = departementRepository.findByCode("IT")
                        .orElseThrow(() -> new RuntimeException("Département IT non trouvé"));

                Employe employe = Employe.builder()
                        .matricule("EMP001")
                        .nom("Dupont")
                        .prenom("Jean")
                        .email("employe@test.com")
                        .password(passwordEncoder.encode("employe123"))
                        .adresse("123 Rue Test")
                        .dateEmbauche(java.time.LocalDate.now())
                        .poste(Employe.Poste.DEVELOPPEUR)
                        .role(Employe.Role.EMPLOYE)
                        .isCompteActif(true)
                        .premiereConnexion(true)
                        .departement(itDepartement)
                        .build();

                employeRepository.save(employe);
                System.out.println("Employé créé avec succès");
            }
        };
    }
}