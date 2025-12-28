package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CongeRepository extends JpaRepository<Conge, Long> {

    // ✅ Méthodes communes
    List<Conge> findByEmployeId(Long employeId);

    List<Conge> findByStatut(Conge.StatutConge statut);

    List<Conge> findByManagerValidateurId(Long managerId);

    List<Conge> findByRhValidateurId(Long rhId);

    @Query("SELECT c FROM Conge c WHERE c.employe.departement.id = :departementId AND c.statut = :statut")
    List<Conge> findByDepartementAndStatut(@Param("departementId") Long departementId,
                                           @Param("statut") Conge.StatutConge statut);

    @Query("SELECT COUNT(c) FROM Conge c WHERE c.employe.id = :employeId AND YEAR(c.dateDebut) = :annee")
    long countCongeByEmployeAndAnnee(@Param("employeId") Long employeId,
                                     @Param("annee") int annee);

    // ✅ Compteurs par département (version 1)
    @Query("SELECT COUNT(c) FROM Conge c WHERE c.employe.departement.id = :departementId")
    long countByDepartement(@Param("departementId") Long departementId);

    @Query("SELECT COUNT(c) FROM Conge c WHERE c.employe.departement.id = :departementId AND c.statut = :statut")
    long countByDepartementAndStatut(@Param("departementId") Long departementId,
                                     @Param("statut") Conge.StatutConge statut);

    // ✅ Historique par département + liste statuts (version 1)
    @Query("""
        SELECT c FROM Conge c
        WHERE c.employe.departement.id = :departementId
          AND c.statut IN :statuts
        ORDER BY c.dateDemande DESC
    """)
    List<Conge> findHistoriqueByDepartement(@Param("departementId") Long departementId,
                                            @Param("statuts") List<Conge.StatutConge> statuts);

    // ✅ Méthodes supplémentaires version 2

    long countByStatut(Conge.StatutConge statut);

    // ⚠️ important : ton entity fusionné a "typeConge" en java et colonne "type_conge"
    long countByTypeConge(Conge.TypeConge typeConge);

    List<Conge> findByEmployeIdOrderByDateDebutDesc(Long employeId);

    boolean existsByEmployeAndDateDebut(Employe employe, LocalDate dateDebut);

    // ✅ Stats Approvals
    @Query("SELECT COUNT(c) FROM Conge c WHERE c.statut = 'VALIDE_RH' AND MONTH(c.dateTraitement) = :mois AND YEAR(c.dateTraitement) = :annee")
    long countApprouvesMois(@Param("mois") int mois, @Param("annee") int annee);

    @Query("SELECT COUNT(c) FROM Conge c WHERE c.statut = 'VALIDE_RH' AND YEAR(c.dateTraitement) = :annee")
    long countApprouvesAnnee(@Param("annee") int annee);

    @Query("SELECT COUNT(c) FROM Conge c WHERE c.statut = 'REFUSE_RH' AND MONTH(c.dateTraitement) = :mois AND YEAR(c.dateTraitement) = :annee")
    long countRefusesMois(@Param("mois") int mois, @Param("annee") int annee);

    // ✅ Stats durée / utilisation
    @Query("SELECT AVG(DATEDIFF(c.dateFin, c.dateDebut) + 1) FROM Conge c WHERE c.statut = 'VALIDE_RH'")
    Double avgDureeConges();

    @Query("SELECT COALESCE(SUM(DATEDIFF(c.dateFin, c.dateDebut) + 1), 0) FROM Conge c WHERE c.employe.id = :employeId AND c.statut = 'VALIDE_RH' AND YEAR(c.dateDebut) = :annee")
    int sumJoursUtilises(@Param("employeId") Long employeId,
                         @Param("annee") int annee);

    // ✅ Absences équipe
    @Query("SELECT c FROM Conge c WHERE c.employe.departement.id = :departementId AND c.statut IN ('VALIDE_MANAGER', 'VALIDE_RH') AND c.dateDebut <= :dateFin AND c.dateFin >= :dateDebut")
    List<Conge> findAbsencesEquipe(@Param("departementId") Long departementId,
                                   @Param("dateDebut") LocalDate dateDebut,
                                   @Param("dateFin") LocalDate dateFin);

    // ✅ Chevauchement (overlap)
    @Query("SELECT COUNT(c) > 0 FROM Conge c WHERE c.employe.id = :employeId AND c.statut NOT IN ('REFUSE_MANAGER', 'REFUSE_RH', 'ANNULE') AND c.dateDebut <= :dateFin AND c.dateFin >= :dateDebut AND c.id != :excludeId")
    boolean existsChevauchement(@Param("employeId") Long employeId,
                                @Param("dateDebut") LocalDate dateDebut,
                                @Param("dateFin") LocalDate dateFin,
                                @Param("excludeId") Long excludeId);

    // ✅ Congés en attente RH triés par priorité
    @Query("SELECT c FROM Conge c WHERE c.statut IN ('VALIDE_MANAGER', 'EN_ATTENTE_RH') ORDER BY c.priorite DESC, c.createdAt ASC")
    List<Conge> findCongesEnAttenteRH();

    // ✅ Top employés par jours utilisés
    @Query("SELECT c.employe.id, c.employe.nom, c.employe.prenom, SUM(DATEDIFF(c.dateFin, c.dateDebut) + 1) as totalJours FROM Conge c WHERE c.statut = 'VALIDE_RH' AND YEAR(c.dateDebut) = :annee GROUP BY c.employe.id, c.employe.nom, c.employe.prenom ORDER BY totalJours DESC")
    List<Object[]> findTopEmployesByConges(@Param("annee") int annee);
}
