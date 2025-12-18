package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeService {
    // Gardez creerEmploye pour la création avec mot de passe automatique
    Employe creerEmploye(Employe employe, String password);

    // Ajoutez saveEmploye pour les mises à jour simples
    Employe saveEmploye(Employe employe);

    // Méthodes existantes
    Optional<Employe> getEmployeById(Long id);
    Optional<Employe> getEmployeByEmail(String email);
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
    Employe updateProfil(Long id, String nom, String prenom, String telephone,
                         String adresse, LocalDate dateNaissance, Employe.Poste poste,
                         Departement departement);
}