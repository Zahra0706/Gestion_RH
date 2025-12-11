package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Annonce;
import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface EmailService {
    void envoyerIdentifiantsEmploye(Employe employe, String password);
    void envoyerNotificationCompteDesactive(Employe employe);
    void envoyerNotificationCompteActive(Employe employe);
    void envoyerNotificationConge(Employe employe, String sujet, String message);

    void envoyerNotificationCongeManager(Conge savedConge);

    void envoyerNotificationCongeRH(Conge conge);

    void envoyerNotificationRefusConge(Conge conge, String manager);

    @Async
    void envoyerNotificationAnnonce(Annonce annonce, Employe recipient);

    @Async
    void envoyerNotificationAnnonceImportante(Annonce annonce, List<Employe> recipients);

    @Async
    void envoyerNotificationAnnonceDepartement(Annonce annonce, Departement departement);
}