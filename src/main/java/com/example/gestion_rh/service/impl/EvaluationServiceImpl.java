package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.Evaluation;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.repository.EvaluationRepository;
import com.example.gestion_rh.service.EvaluationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final EmployeRepository employeRepository;

    public EvaluationServiceImpl(EvaluationRepository evaluationRepository,
                                 EmployeRepository employeRepository) {
        this.evaluationRepository = evaluationRepository;
        this.employeRepository = employeRepository;
    }

    @Override
    public Evaluation remplirEvaluation(Long evaluationId, Long managerId,
                                        double scorePerformance, double scoreCompetences,
                                        String commentairesManager) {

        Employe manager = employeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager introuvable"));

        Evaluation eval = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Évaluation introuvable"));

        Long deptManager = manager.getDepartement() != null ? manager.getDepartement().getId() : null;
        Long deptEmploye = eval.getEmploye().getDepartement() != null ? eval.getEmploye().getDepartement().getId() : null;

        if (deptManager == null || deptEmploye == null || !deptManager.equals(deptEmploye)) {
            throw new RuntimeException("Accès interdit : autre département");
        }

        if (eval.getStatut() == Evaluation.StatutEvaluation.ARCHIVEE) {
            throw new RuntimeException("Évaluation archivée, modification impossible");
        }

        if (eval.getManager() == null) {
            eval.setManager(manager);
        }

        eval.setScorePerformance(scorePerformance);
        eval.setScoreCompetences(scoreCompetences);

        eval.setScoreGlobal((scorePerformance + scoreCompetences) / 2.0);

        eval.setCommentairesManager(commentairesManager);
        eval.setDateCompletion(LocalDate.now());
        eval.setStatut(Evaluation.StatutEvaluation.COMPLETEE);

        return evaluationRepository.save(eval);
    }
}
