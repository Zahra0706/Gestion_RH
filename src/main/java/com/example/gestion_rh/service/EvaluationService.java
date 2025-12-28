package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.EvaluationDTO;
import com.example.gestion_rh.entity.Evaluation;
import com.example.gestion_rh.entity.EvaluationCampagne;

import java.util.List;
import java.util.Map;

public interface EvaluationService {

    // ✅ Méthode simple (de la première version)
    Evaluation remplirEvaluation(Long evaluationId, Long managerId,
                                 double scorePerformance, double scoreCompetences,
                                 String commentairesManager);

    // ==========================
    // ✅ Campaign Management
    // ==========================
    EvaluationCampagne creerCampagne(EvaluationCampagne campagne, Long rhId);

    List<Evaluation> genererEvaluationsPourCampagne(Long campagneId);

    List<Evaluation> genererEvaluationsGroupees(Long campagneId, Long managerId, List<Long> employeIds);

    void envoyerEvaluationsAuxManagers(Long campagneId);

    void envoyerRappelManagers(List<Long> managerIds);

    // ==========================
    // ✅ Evaluation Management
    // ==========================
    Evaluation sauvegarderEvaluation(Long evaluationId, EvaluationDTO evaluationDTO);

    Evaluation getEvaluationById(Long id);

    List<Evaluation> getEvaluationsByManager(Long managerId);

    List<Evaluation> getEvaluationsByCampagne(Long campagneId);

    List<Evaluation> getEvaluationsByEmploye(Long employeId);

    void assignerManager(Long evaluationId, Long managerId);

    Evaluation saveEvaluation(Evaluation evaluation);

    void supprimerEvaluation(Long id);

    // ==========================
    // ✅ Statistics / Reporting
    // ==========================
    Map<String, Object> calculerStatistiquesCampagne(Long campagneId);

    Map<String, Long> getRepartitionParCategorie(Long campagneId);

    // ==========================
    // ✅ Scoring
    // ==========================
    Double calculerScoreGlobal(Evaluation evaluation);

    Double calculerScoreGlobal(Double scorePerformance, Double scoreCompetences, Double scoreObjectifs);

    String getCategoriePerformance(Double score);
}
