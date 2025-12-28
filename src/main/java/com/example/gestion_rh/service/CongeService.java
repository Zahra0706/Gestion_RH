package com.example.gestion_rh.service;

import com.example.gestion_rh.dto.CalendarEventDTO;
import com.example.gestion_rh.dto.CongeStatsDTO;
import com.example.gestion_rh.dto.CongeValidationDTO;
import com.example.gestion_rh.dto.RhAlertDTO;
import com.example.gestion_rh.entity.Conge;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CongeService {

    // ==========================
    // ✅ Méthodes principales
    // ==========================
    Conge demanderConge(Conge conge);

    Conge validerCongeManager(Long congeId, Long managerId, boolean validation, String commentaire);

    Conge validerCongeRH(Long congeId, Long rhId, boolean validation, String commentaire);

    List<Conge> getCongesByEmploye(Long employeId);

    List<Conge> getCongesEnAttenteManager(Long managerId);

    List<Conge> getCongesEnAttenteRH();

    List<Conge> getCongesByStatut(Conge.StatutConge statut);

    Conge annulerConge(Long congeId, Long employeId);

    double calculerJoursCongesRestants(Long employeId, int annee);


    // ==========================
    // ✅ Manager - Congés département
    // ==========================
    List<Conge> getCongesDepartementByStatut(Long departementId, Conge.StatutConge statut);

    List<Conge> getHistoriqueCongesDepartement(Long departementId);


    // ==========================
    // ✅ Méthodes avancées (Admin/RH)
    // ==========================
    Conge mettreEnAttenteRH(Long congeId, Long rhId, String commentaire);

    Conge renvoyerAuManager(Long congeId, Long rhId, String commentaire);

    Optional<Conge> getCongeById(Long id);

    CongeStatsDTO getStatistiques();

    CongeValidationDTO getValidationData(Long congeId);

    List<Conge> getHistoriqueEmploye(Long employeId);

    List<Conge> getAbsencesEquipe(Long departementId, LocalDate dateDebut, LocalDate dateFin);

    boolean checkChevauchement(Long employeId, LocalDate dateDebut, LocalDate dateFin, Long excludeId);

    List<Object[]> getTopEmployeesByConges(int annee);

    List<Conge> getAllConges();

    List<Object[]> getTendancesMensuelles(int annee);

    List<RhAlertDTO> getAlertes();

    List<Conge> getPendingOver48h();

    List<CalendarEventDTO> getCalendarEvents(int month, int year);

    List<RhAlertDTO> getConflicts(LocalDate dateDebut, LocalDate dateFin, int threshold);


    // ==========================
    // ✅ Solde congés (optionnel)
    // ==========================
    int getCongesUtilises(Long employeId);

    int getCongesRestants(Long employeId);
}
