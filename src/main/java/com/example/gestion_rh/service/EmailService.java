package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;

public interface EmailService {
    void envoyerIdentifiantsEmploye(Employe employe, String password);
    void envoyerNotificationCompteDesactive(Employe employe);
    void envoyerNotificationCompteActive(Employe employe);
    void envoyerNotificationConge(Employe employe, String sujet, String message);

    void envoyerNotificationCongeManager(Conge savedConge);

    void envoyerNotificationCongeRH(Conge conge);

    void envoyerNotificationRefusConge(Conge conge, String manager);
}