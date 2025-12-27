package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Conge;
import java.time.LocalDate;

public interface CongeDisponibiliteIntegrationService {

    // Sync single leave
    void synchroniserConge(Conge conge);

    // Clear sync for a leave (when cancelled/refused)
    void annulerSynchronisationConge(Conge conge);

    // Check conflicts before leave validation
    boolean verifierConflits(Long employeId, LocalDate debut, LocalDate fin);
}
