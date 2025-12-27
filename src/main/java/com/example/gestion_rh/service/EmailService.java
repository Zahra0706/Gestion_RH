package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Annonce;
import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.Salaire;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface EmailService {
    // Méthodes communes aux deux fichiers
    void envoyerIdentifiantsEmploye(Employe employe, String password);
    void envoyerNotificationCompteDesactive(Employe employe);
    void envoyerNotificationCompteActive(Employe employe);
    void envoyerNotificationConge(Employe employe, String sujet, String message);
    void envoyerNotificationCongeManager(Conge conge);
    void envoyerNotificationCongeRH(Conge conge);
    void envoyerNotificationRefusConge(Conge conge, String source);

    // Méthodes du premier fichier uniquement
    @Async
    void envoyerNotificationAnnonce(Annonce annonce, Employe recipient);

    @Async
    void envoyerNotificationAnnonceImportante(Annonce annonce, List<Employe> recipients);

    @Async
    void envoyerNotificationAnnonceDepartement(Annonce annonce, Departement departement);

    // Méthodes du deuxième fichier uniquement
    void envoyerContratParEmail(String destinataire, String sujet, String contenu,
                                byte[] pdfAttachment, String nomFichier);

    void envoyerEmail(String destinataire, String sujet, String contenu);

    void envoyerRapport(String destinataire, String sujet, String contenu, byte[] attachment, String nomFichier);

    void envoyerFicheDePaie(Salaire salaire, Employe employe);

    void envoyerRappelSalaireNonValide(Employe rh, List<Salaire> salairesEnAttente);

    void envoyerConfirmationPaiement(Salaire salaire);
}