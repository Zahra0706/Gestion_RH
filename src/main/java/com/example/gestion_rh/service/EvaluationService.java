package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.EvaluationDTO;
import com.example.gestion_rh.entity.Evaluation;
import com.example.gestion_rh.entity.EvaluationCampagne;

import java.util.List;
import java.util.Map;

public interface EvaluationService {

    // Campaign Management
    EvaluationCampagne creerCampagne(EvaluationCampagne campagne, Long rhId);

    List<Evaluation> genererEvaluationsPourCampagne(Long campagneId);

    void envoyerEvaluationsAuxManagers(Long campagneId);

    // Evaluation Management
    Evaluation sauvegarderEvaluation(Long evaluationId, EvaluationDTO evaluationDTO);

    Evaluation getEvaluationById(Long id);

    List<Evaluation> getEvaluationsByManager(Long managerId);

    List<Evaluation> getEvaluationsByCampagne(Long campagneId);

    List<Evaluation> getEvaluationsByEmploye(Long employeId);

    // Statistics and Reporting
    Map<String, Object> calculerStatistiquesCampagne(Long campagneId);

    List<Evaluation> getEvaluationsEnRetard();

    void envoyerRappelManagers(List<Long> managerIds);

    // Scoring
    Double calculerScoreGlobal(Evaluation evaluation);

    String getCategoriePerformance(Double score);

    Map<String, Long> getRepartitionParCategorie(Long campagneId);

    void assignerManager(Long evaluationId, Long managerId);

    List<Evaluation> genererEvaluationsGroupees(Long campagneId, Long managerId, List<Long> employeIds);

    Evaluation saveEvaluation(Evaluation evaluation);

    Double calculerScoreGlobal(Double scorePerformance, Double scoreCompetences, Double scoreObjectifs);

    void supprimerEvaluation(Long id);
}
