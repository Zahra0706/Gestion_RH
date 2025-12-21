package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Conge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CongeRepository extends JpaRepository<Conge, Long> {
    List<Conge> findByEmployeId(Long employeId);

    List<Conge> findByStatut(Conge.StatutConge statut);

    List<Conge> findByManagerValidateurId(Long managerId);

    List<Conge> findByRhValidateurId(Long rhId);

    @Query("SELECT c FROM Conge c WHERE c.employe.departement.id = :departementId AND c.statut = :statut")
    List<Conge> findByDepartementAndStatut(@Param("departementId") Long departementId,
            @Param("statut") Conge.StatutConge statut);

    @Query("SELECT COUNT(c) FROM Conge c WHERE c.employe.id = :employeId AND YEAR(c.dateDebut) = :annee")
    long countCongeByEmployeAndAnnee(@Param("employeId") Long employeId, @Param("annee") int annee);

    // ============ NEW STATISTICS QUERIES ============

    // Count by status
    long countByStatut(Conge.StatutConge statut);

    // Count approved this month
    @Query("SELECT COUNT(c) FROM Conge c WHERE c.statut = 'VALIDE_RH' AND MONTH(c.dateTraitement) = :mois AND YEAR(c.dateTraitement) = :annee")
    long countApprouvesMois(@Param("mois") int mois, @Param("annee") int annee);

    // Count approved this year
    @Query("SELECT COUNT(c) FROM Conge c WHERE c.statut = 'VALIDE_RH' AND YEAR(c.dateTraitement) = :annee")
    long countApprouvesAnnee(@Param("annee") int annee);

    // Count refused this month
    @Query("SELECT COUNT(c) FROM Conge c WHERE c.statut = 'REFUSE_RH' AND MONTH(c.dateTraitement) = :mois AND YEAR(c.dateTraitement) = :annee")
    long countRefusesMois(@Param("mois") int mois, @Param("annee") int annee);

    // Count by type
    long countByTypeConge(Conge.TypeConge typeConge);

    // Average duration of approved leaves
    @Query("SELECT AVG(DATEDIFF(c.dateFin, c.dateDebut) + 1) FROM Conge c WHERE c.statut = 'VALIDE_RH'")
    Double avgDureeConges();

    // Employee history ordered by date
    List<Conge> findByEmployeIdOrderByDateDebutDesc(Long employeId);

    // Count days used by employee in a year
    @Query("SELECT COALESCE(SUM(DATEDIFF(c.dateFin, c.dateDebut) + 1), 0) FROM Conge c WHERE c.employe.id = :employeId AND c.statut = 'VALIDE_RH' AND YEAR(c.dateDebut) = :annee")
    int sumJoursUtilises(@Param("employeId") Long employeId, @Param("annee") int annee);

    // Team absences in a date range
    @Query("SELECT c FROM Conge c WHERE c.employe.departement.id = :departementId AND c.statut IN ('VALIDE_MANAGER', 'VALIDE_RH') AND c.dateDebut <= :dateFin AND c.dateFin >= :dateDebut")
    List<Conge> findAbsencesEquipe(@Param("departementId") Long departementId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);

    // Check for overlapping leaves
    @Query("SELECT COUNT(c) > 0 FROM Conge c WHERE c.employe.id = :employeId AND c.statut NOT IN ('REFUSE_MANAGER', 'REFUSE_RH', 'ANNULE') AND c.dateDebut <= :dateFin AND c.dateFin >= :dateDebut AND c.id != :excludeId")
    boolean existsChevauchement(@Param("employeId") Long employeId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("excludeId") Long excludeId);

    // Pending RH validation (both statuses)
    @Query("SELECT c FROM Conge c WHERE c.statut IN ('VALIDE_MANAGER', 'EN_ATTENTE_RH') ORDER BY c.priorite DESC, c.createdAt ASC")
    List<Conge> findCongesEnAttenteRH();

    // Top employees by leave days
    @Query("SELECT c.employe.id, c.employe.nom, c.employe.prenom, SUM(DATEDIFF(c.dateFin, c.dateDebut) + 1) as totalJours FROM Conge c WHERE c.statut = 'VALIDE_RH' AND YEAR(c.dateDebut) = :annee GROUP BY c.employe.id, c.employe.nom, c.employe.prenom ORDER BY totalJours DESC")
    List<Object[]> findTopEmployesByConges(@Param("annee") int annee);

    long count();
}
