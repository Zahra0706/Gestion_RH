package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartementRepository extends JpaRepository<Departement, Long> {

    Optional<Departement> findByCode(String code);
    Optional<Departement> findByNom(String nom);
    List<Departement> findByManagerId(Long managerId);

    // Méthodes pour rechercher par statut d'activation
    List<Departement> findByIsActifTrue();
    List<Departement> findByIsActifFalse();

    // Recherche avec jointure
    @Query("SELECT d FROM Departement d WHERE d.isActif = :isActif")
    List<Departement> findByStatut(@Param("isActif") Boolean isActif);

    // Compter les départements actifs
    @Query("SELECT COUNT(d) FROM Departement d WHERE d.isActif = true")
    long countDepartementsActifs();

    // Compter les départements inactifs
    @Query("SELECT COUNT(d) FROM Departement d WHERE d.isActif = false")
    long countDepartementsInactifs();

    // Rechercher les départements sans manager
    @Query("SELECT d FROM Departement d WHERE d.manager IS NULL AND d.isActif = true")
    List<Departement> findDepartementsSansManager();

    // Rechercher les départements par nom (avec LIKE)
    @Query("SELECT d FROM Departement d WHERE LOWER(d.nom) LIKE LOWER(CONCAT('%', :nom, '%'))")
    List<Departement> searchByNom(@Param("nom") String nom);
}