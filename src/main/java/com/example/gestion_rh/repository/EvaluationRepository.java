package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    // ✅ Commun
    List<Evaluation> findByEmployeId(Long employeId);

    List<Evaluation> findByManagerId(Long managerId);

    // ✅ Version 1 : recherche par année + statut
    List<Evaluation> findByAnneeAndStatut(Integer annee, Evaluation.StatutEvaluation statut);

    Optional<Evaluation> findByEmployeIdAndAnnee(Long employeId, Integer annee);

    // ✅ Version 1 : filtrage par département
    List<Evaluation> findByEmploye_Departement_IdOrderByAnneeDesc(Long departementId);

    Optional<Evaluation> findByIdAndEmploye_Departement_Id(Long id, Long departementId);

    // ✅ Moyenne score global
    @Query("SELECT AVG(e.scoreGlobal) FROM Evaluation e WHERE e.annee = :annee AND e.employe.departement.id = :departementId")
    Double findMoyenneScoreByDepartementAndAnnee(@Param("departementId") Long departementId,
                                                 @Param("annee") Integer annee);

    // ✅ Version 2 : campagne
    List<Evaluation> findByCampagneId(Long campagneId);

    Optional<Evaluation> findByEmployeIdAndCampagneId(Long employeId, Long campagneId);

    // ✅ Version 2 : statut direct
    List<Evaluation> findByStatut(Evaluation.StatutEvaluation statut);
}
