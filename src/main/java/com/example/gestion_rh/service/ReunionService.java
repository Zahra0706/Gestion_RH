package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Reunion;

import java.util.List;

/**
 * Service de lecture des réunions.
 *
 * Objectif ici : permettre à l'employé de consulter les réunions déjà planifiées
 * dans les autres branches (Manager/RH/Admin) en réutilisant le même modèle.
 */
public interface ReunionService {

    /**
     * Renvoie les réunions visibles pour un employé :
     * - réunions où il est participant
     * - réunions de son département
     * - réunions générales (département = null)
     */
    List<Reunion> getReunionsVisiblesPourEmploye(Long employeId, Long departementId);
}
