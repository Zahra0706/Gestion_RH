package com.example.gestion_rh.service;


import com.example.gestion_rh.entity.Conge;
import java.util.List;

public interface CongeService {
    Conge demanderConge(Conge conge);
    Conge validerCongeManager(Long congeId, Long managerId, boolean validation, String commentaire);
    Conge validerCongeRH(Long congeId, Long rhId, boolean validation, String commentaire);
    List<Conge> getCongesByEmploye(Long employeId);
    List<Conge> getCongesEnAttenteManager(Long managerId);
    List<Conge> getCongesEnAttenteRH();
    List<Conge> getCongesByStatut(Conge.StatutConge statut);
    Conge annulerConge(Long congeId, Long employeId);
    double calculerJoursCongesRestants(Long employeId, int annee);
}