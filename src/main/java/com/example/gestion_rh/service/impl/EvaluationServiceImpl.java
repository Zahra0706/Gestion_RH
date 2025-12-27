package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Evaluation;
import com.example.gestion_rh.repository.EvaluationRepository;
import com.example.gestion_rh.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;

    @Override
    public List<Evaluation> getEvaluationsByEmploye(Long employeId) {
        return evaluationRepository.findByEmployeId(employeId);
    }


    @Override
    public Evaluation getEvaluationById(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Évaluation introuvable"));
    }
}
