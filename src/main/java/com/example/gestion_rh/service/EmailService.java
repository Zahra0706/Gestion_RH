package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;

/**
 * Service for sending emails
 */
public interface EmailService {

    /**
     * Send contract by email with PDF attachment
     * 
     * @param destinataire  Email address of recipient
     * @param sujet         Email subject
     * @param contenu       Email body content
     * @param pdfAttachment PDF file content as byte array
     * @param nomFichier    Attachment filename
     */
    void envoyerContratParEmail(String destinataire, String sujet, String contenu,
            byte[] pdfAttachment, String nomFichier);

    /**
     * Send simple email without attachment
     * 
     * @param destinataire Email address
     * @param sujet        Subject
     * @param contenu      Body content
     */
    void envoyerEmail(String destinataire, String sujet, String contenu);

    // Notification methods for Conge (Leave) management
    void envoyerNotificationCongeManager(Conge conge);

    void envoyerNotificationCongeRH(Conge conge);

    void envoyerNotificationRefusConge(Conge conge, String source);

    void envoyerNotificationConge(Employe employe, String sujet, String message);

    // Notification methods for Employe (Employee) management
    void envoyerIdentifiantsEmploye(Employe employe, String password);

    void envoyerNotificationCompteDesactive(Employe employe);

    void envoyerNotificationCompteActive(Employe employe);

    // Report sending
    void envoyerRapport(String destinataire, String sujet, String contenu, byte[] attachment, String nomFichier);

    // Salary Management
    void envoyerFicheDePaie(com.example.gestion_rh.entity.Salaire salaire, Employe employe);

    void envoyerRappelSalaireNonValide(Employe rh,
            java.util.List<com.example.gestion_rh.entity.Salaire> salairesEnAttente);

    void envoyerConfirmationPaiement(com.example.gestion_rh.entity.Salaire salaire);
}