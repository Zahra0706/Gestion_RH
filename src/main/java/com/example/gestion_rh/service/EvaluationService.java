package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Evaluation;

public interface EvaluationService {
    Evaluation remplirEvaluation(Long evaluationId, Long managerId,
                                 double scorePerformance, double scoreCompetences,
                                 String commentairesManager);
}
