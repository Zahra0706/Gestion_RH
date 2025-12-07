package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;

public interface EmailService {
    void envoyerIdentifiantsEmploye(Employe employe, String password);

    void envoyerNotificationCompteDesactive(Employe employe);

    void envoyerNotificationCongeManager(Conge conge);
    void envoyerNotificationCongeRH(Conge conge);
    void envoyerNotificationRefusConge(Conge conge, String sourceValidation);
    void envoyerNotificationGenerique(String destinataire, String sujet, String contenu);

    void envoyerNotificationCompteActive(Employe employe);

    void envoyerEmailGenerique(String destinataire, String sujet, String contenu);
}
