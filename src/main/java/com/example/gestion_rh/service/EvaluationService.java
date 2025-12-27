package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Evaluation;

import java.util.List;

public interface EvaluationService {
    List<Evaluation> getEvaluationsByEmploye(Long employeId);
    Evaluation getEvaluationById(Long id);
}
