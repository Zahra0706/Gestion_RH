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

    List<Evaluation> findByEmployeId(Long employeId);
    List<Evaluation> findByManagerId(Long managerId);
    List<Evaluation> findByAnneeAndStatut(int annee, Evaluation.StatutEvaluation statut);
    Optional<Evaluation> findByEmployeIdAndAnnee(Long employeId, int annee);

    List<Evaluation> findByEmploye_Departement_IdOrderByAnneeDesc(Long departementId);
    Optional<Evaluation> findByIdAndEmploye_Departement_Id(Long id, Long departementId);

    @Query("SELECT AVG(e.scoreGlobal) FROM Evaluation e WHERE e.annee = :annee AND e.employe.departement.id = :departementId")
    Double findMoyenneScoreByDepartementAndAnnee(@Param("departementId") Long departementId,
                                                 @Param("annee") int annee);
}


