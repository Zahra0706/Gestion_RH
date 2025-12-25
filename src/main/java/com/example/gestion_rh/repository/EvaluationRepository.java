package com.example.gestion_rh.repository;

import com.example.gestion_rh.entity.Evaluation;
import com.example.gestion_rh.entity.StatutEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByEmployeId(Long employeId);

    List<Evaluation> findByManagerId(Long managerId);

    List<Evaluation> findByCampagneId(Long campagneId);

    List<Evaluation> findByStatut(StatutEvaluation statut);

    Optional<Evaluation> findByEmployeIdAndCampagneId(Long employeId, Long campagneId);
}
