package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.DisponibiliteEmploye;
import com.example.gestion_rh.entity.StatutDisponibilite;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DisponibiliteService {

    // Get availability for a date
    List<DisponibiliteEmploye> getDisponibilites(LocalDate date);

    // Get availability for a department on a date
    List<DisponibiliteEmploye> getDisponibilitesParDepartement(Long departementId, LocalDate date);

    // Manual override
    DisponibiliteEmploye modifierStatut(Long employeId, LocalDate date, StatutDisponibilite statut, String raison,
            String commentaire, Long modifieParId);

    // Apply status for a period
    void appliquerStatutPeriode(Long employeId, LocalDate debut, LocalDate fin, StatutDisponibilite statut,
            String raison, Long modifieParId);

    // Statistics
    Map<String, Object> getStatistiquesPresence(LocalDate date);

    // Rule processing
    void traiterReglesAutomatiques(LocalDate date);
}
