package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Salaire;
import com.example.gestion_rh.repository.DepartementRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.repository.spec.EmployeSpecification;
import com.example.gestion_rh.service.EmailService;
import com.example.gestion_rh.service.EmployeService;
import com.example.gestion_rh.service.PasswordGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeServiceImpl implements EmployeService {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;
    private final EmailService emailService;

    // =====================================================
    // ✅ CRUD BASIQUES
    // =====================================================

    @Override
    public Optional<Employe> getEmployeById(Long id) {
        return employeRepository.findById(id);
    }

    @Override
    public Optional<Employe> getEmployeByEmail(String email) {
        return employeRepository.findByEmail(email);
    }

    @Override
    public Optional<Employe> findByEmail(String email) {
        return employeRepository.findByEmail(email);
    }

    @Override
    public List<Employe> getAllEmployes() {
        return employeRepository.findAll();
    }

    @Override
    public List<Employe> getEmployesByDepartement(Long departementId) {
        return employeRepository.findByDepartementId(departementId);
    }

    @Override
    public List<Employe> getEmployesByRole(Employe.Role role) {
        return employeRepository.findByRole(role);
    }

    // =====================================================
    // ✅ RECHERCHE + FILTER AVANCÉ (specification)
    // =====================================================
    @Override
    public Page<Employe> getEmployes(String keyword, Long departementId, Employe.Role role, Boolean isActif,
                                     Pageable pageable) {
        Specification<Employe> spec = EmployeSpecification.getSpec(keyword, departementId, role, isActif);
        return employeRepository.findAll(spec, pageable);
    }

    // =====================================================
    // ✅ UPDATE
    // =====================================================
    @Override
    public Employe updateEmploye(Long id, Employe employe) {

        Employe existingEmploye = employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        existingEmploye.setNom(employe.getNom());
        existingEmploye.setPrenom(employe.getPrenom());
        existingEmploye.setEmail(employe.getEmail());
        existingEmploye.setTelephone(employe.getTelephone());
        existingEmploye.setAdresse(employe.getAdresse());
        existingEmploye.setPoste(employe.getPoste());
        existingEmploye.setRole(employe.getRole());
        existingEmploye.setDepartement(employe.getDepartement());

        // ✅ Si rôle MANAGER => affecter comme manager du département
        if (employe.getRole() == Employe.Role.MANAGER && employe.getDepartement() != null) {
            Departement departement = departementRepository.findById(employe.getDepartement().getId())
                    .orElseThrow(() -> new RuntimeException("Département non trouvé"));
            departement.setManager(existingEmploye);
            departementRepository.save(departement);
        }

        return employeRepository.save(existingEmploye);
    }

    // =====================================================
    // ✅ DELETE
    // =====================================================
    @Override
    public void deleteEmploye(Long id) {

        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        // ✅ si c'est manager de son département => retirer
        if (employe.getDepartement() != null
                && employe.getDepartement().getManager() != null
                && employe.getDepartement().getManager().getId().equals(id)) {

            Departement departement = employe.getDepartement();
            departement.setManager(null);
            departementRepository.save(departement);
        }

        employeRepository.deleteById(id);
    }

    // =====================================================
    // ✅ ACTIVATION / DÉSACTIVATION
    // =====================================================
    @Override
    public void desactiverCompte(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        employe.setIsCompteActif(false);
        employeRepository.save(employe);

        emailService.envoyerNotificationCompteDesactive(employe);
    }

    @Override
    public void activerCompte(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        employe.setIsCompteActif(true);
        employeRepository.save(employe);

        emailService.envoyerNotificationCompteActive(employe);
    }

    // =====================================================
    // ✅ PASSWORD
    // =====================================================
    @Override
    public void reinitialiserMotDePasse(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        String nouveauMotDePasse = passwordGeneratorService.generateRandomPassword(12);
        employe.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        employe.setPremiereConnexion(true);

        employeRepository.save(employe);

        emailService.envoyerIdentifiantsEmploye(employe, nouveauMotDePasse);
    }

    @Override
    public Employe changerMotDePasse(Long id, String nouveauMotDePasse) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        employe.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        employe.setPremiereConnexion(false);

        return employeRepository.save(employe);
    }

    // =====================================================
    // ✅ STATS
    // =====================================================
    @Override
    public long countEmployesByDepartement(Long departementId) {
        return employeRepository.countByDepartement(departementId);
    }

    @Override
    public List<Employe> getEmployesActifs() {
        return employeRepository.findByIsCompteActifTrue();
    }

    @Override
    public List<Employe> getEmployesInactifs() {
        return employeRepository.findByIsCompteActifFalse();
    }

    // =====================================================
    // ✅ CREATION EMPLOYÉ (ADMIN / RH)
    // =====================================================
    @Override
    public Employe saveEmploye(Employe employe) {

        if (employe.getMatricule() != null &&
                employeRepository.findByMatricule(employe.getMatricule()).isPresent()) {
            throw new RuntimeException("Matricule déjà utilisé");
        }

        if (employe.getEmail() != null &&
                employeRepository.findByEmail(employe.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        return employeRepository.save(employe);
    }

    @Override
    public Employe creerEmploye(Employe employe, String password) {

        if (employeRepository.findByMatricule(employe.getMatricule()).isPresent()) {
            throw new RuntimeException("Matricule déjà utilisé");
        }

        if (employeRepository.findByEmail(employe.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // ✅ mot de passe auto si absent
        if (password == null || password.isEmpty()) {
            password = passwordGeneratorService.generateRandomPassword(12);
        }

        employe.setPassword(passwordEncoder.encode(password));

        if (employe.getIsCompteActif() == null) employe.setIsCompteActif(true);
        if (employe.getPremiereConnexion() == null) employe.setPremiereConnexion(true);

        // ✅ manager => affecter département
        if (employe.getDepartement() != null && employe.getRole() == Employe.Role.MANAGER) {
            Departement departement = departementRepository.findById(employe.getDepartement().getId())
                    .orElseThrow(() -> new RuntimeException("Département non trouvé"));
            departement.setManager(employe);
            departementRepository.save(departement);
        }

        Employe saved = employeRepository.save(employe);

        emailService.envoyerIdentifiantsEmploye(saved, password);

        return saved;
    }

    // =====================================================
    // ✅ UPDATE PROFIL
    // =====================================================
    @Override
    public Employe updateProfil(Long id, String nom, String prenom, String telephone,
                                String adresse, LocalDate dateNaissance, Employe.Poste poste,
                                Departement departement) {

        Employe existingEmploye = employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        existingEmploye.setNom(nom);
        existingEmploye.setPrenom(prenom);
        existingEmploye.setTelephone(telephone);
        existingEmploye.setAdresse(adresse);
        existingEmploye.setDateNaissance(dateNaissance);
        existingEmploye.setPoste(poste);

        if (departement != null) {
            existingEmploye.setDepartement(departement);
        }

        return employeRepository.save(existingEmploye);
    }

    // =====================================================
    // ✅ DELEGATION NOTIF CONGE/SALAIRE (OPTIONNEL selon ton interface)
    // =====================================================
    @Override
    public void envoyerNotificationCongeManager(Conge conge) {
        emailService.envoyerNotificationCongeManager(conge);
    }

    @Override
    public void envoyerNotificationCongeRH(Conge conge) {
        emailService.envoyerNotificationCongeRH(conge);
    }

    @Override
    public void envoyerNotificationRefusConge(Conge conge, String source) {
        emailService.envoyerNotificationRefusConge(conge, source);
    }

    @Override
    public void envoyerNotificationConge(Employe employe, String sujet, String message) {
        emailService.envoyerNotificationConge(employe, sujet, message);
    }

    @Override
    public void envoyerIdentifiantsEmploye(Employe employe, String password) {
        emailService.envoyerIdentifiantsEmploye(employe, password);
    }

    @Override
    public void envoyerNotificationCompteDesactive(Employe employe) {
        emailService.envoyerNotificationCompteDesactive(employe);
    }

    @Override
    public void envoyerNotificationCompteActive(Employe employe) {
        emailService.envoyerNotificationCompteActive(employe);
    }

    @Override
    public void envoyerContratParEmail(String destinataire, String sujet, String contenu,
                                       byte[] pdfAttachment, String nomFichier) {
        emailService.envoyerContratParEmail(destinataire, sujet, contenu, pdfAttachment, nomFichier);
    }

    @Override
    public void envoyerEmail(String destinataire, String sujet, String contenu) {
        emailService.envoyerEmail(destinataire, sujet, contenu);
    }

    @Override
    public void envoyerRapport(String destinataire, String sujet, String contenu,
                               byte[] attachment, String nomFichier) {
        emailService.envoyerRapport(destinataire, sujet, contenu, attachment, nomFichier);
    }

    @Override
    public void envoyerFicheDePaie(Salaire salaire, Employe employe) {
        emailService.envoyerFicheDePaie(salaire, employe);
    }

    @Override
    public void envoyerRappelSalaireNonValide(Employe rh, List<Salaire> salairesEnAttente) {
        emailService.envoyerRappelSalaireNonValide(rh, salairesEnAttente);
    }

    @Override
    public void envoyerConfirmationPaiement(Salaire salaire) {
        emailService.envoyerConfirmationPaiement(salaire);
    }
}
