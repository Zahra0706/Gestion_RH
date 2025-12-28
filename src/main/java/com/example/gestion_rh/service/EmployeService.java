package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.Salaire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeService {

    // ✅ Pagination + Filtrage (de la version avancée)
    Page<Employe> getEmployes(String keyword, Long departementId, Employe.Role role,
                              Boolean isActif, Pageable pageable);

    // ✅ Création employé (avec mot de passe automatique)
    Employe creerEmploye(Employe employe, String password);

    // ✅ Sauvegarde simple (update)
    Employe saveEmploye(Employe employe);

    // ✅ Méthodes standard
    Optional<Employe> getEmployeById(Long id);

    Optional<Employe> getEmployeByEmail(String email);

    Optional<Employe> findByEmail(String email);

    List<Employe> getAllEmployes();

    List<Employe> getEmployesByDepartement(Long departementId);

    List<Employe> getEmployesByRole(Employe.Role role);

    Employe updateEmploye(Long id, Employe employe);

    void deleteEmploye(Long id);

    void desactiverCompte(Long id);

    void activerCompte(Long id);

    Employe changerMotDePasse(Long id, String nouveauMotDePasse);

    void reinitialiserMotDePasse(Long id);

    long countEmployesByDepartement(Long departementId);

    List<Employe> getEmployesActifs();

    List<Employe> getEmployesInactifs();

    // ✅ Mise à jour du profil (ajouté dans la version avancée)
    Employe updateProfil(Long id, String nom, String prenom, String telephone,
                         String adresse, LocalDate dateNaissance,
                         Employe.Poste poste, Departement departement);

    // ✅ Notifications liées aux congés
    void envoyerNotificationCongeManager(Conge conge);

    void envoyerNotificationCongeRH(Conge conge);

    void envoyerNotificationRefusConge(Conge conge, String source);

    void envoyerNotificationConge(Employe employe, String sujet, String message);

    // ✅ Emails liés au compte
    void envoyerIdentifiantsEmploye(Employe employe, String password);

    void envoyerNotificationCompteDesactive(Employe employe);

    void envoyerNotificationCompteActive(Employe employe);

    // ✅ Emails contrats / rapport
    void envoyerContratParEmail(String destinataire, String sujet, String contenu,
                                byte[] pdfAttachment, String nomFichier);

    void envoyerEmail(String destinataire, String sujet, String contenu);

    void envoyerRapport(String destinataire, String sujet, String contenu,
                        byte[] attachment, String nomFichier);

    // ✅ Emails salaires
    void envoyerFicheDePaie(Salaire salaire, Employe employe);

    void envoyerRappelSalaireNonValide(Employe rh, List<Salaire> salairesEnAttente);

    void envoyerConfirmationPaiement(Salaire salaire);
}
