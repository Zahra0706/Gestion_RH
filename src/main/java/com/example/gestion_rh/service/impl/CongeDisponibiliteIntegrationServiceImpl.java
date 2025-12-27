package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.DisponibiliteEmploye;
import com.example.gestion_rh.entity.StatutDisponibilite;
import com.example.gestion_rh.repository.DisponibiliteRepository;
import com.example.gestion_rh.service.CongeDisponibiliteIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CongeDisponibiliteIntegrationServiceImpl implements CongeDisponibiliteIntegrationService {

    private final DisponibiliteRepository disponibiliteRepository;

    @Override
    public void synchroniserConge(Conge conge) {
        if (conge.getStatut() != Conge.StatutConge.VALIDE_RH) {
            return;
        }

        for (LocalDate date = conge.getDateDebut(); !date.isAfter(conge.getDateFin()); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            DisponibiliteEmploye dispo = disponibiliteRepository
                    .findByEmployeIdAndDate(conge.getEmploye().getId(), date)
                    .orElseGet(() -> DisponibiliteEmploye.builder()
                            .employe(conge.getEmploye())
                            .date(currentDate)
                            .build());

            dispo.setStatut(StatutDisponibilite.CONGE);
            dispo.setCongeLien(conge);
            dispo.setEstPlanifie(true);
            dispo.setRaison("Congé validé: " + conge.getTypeConge());

            disponibiliteRepository.save(dispo);
        }
    }

    @Override
    public void annulerSynchronisationConge(Conge conge) {
        for (LocalDate date = conge.getDateDebut(); !date.isAfter(conge.getDateFin()); date = date.plusDays(1)) {
            Optional<DisponibiliteEmploye> dispoOpt = disponibiliteRepository
                    .findByEmployeIdAndDate(conge.getEmploye().getId(), date);
            if (dispoOpt.isPresent()) {
                DisponibiliteEmploye dispo = dispoOpt.get();
                if (conge.equals(dispo.getCongeLien())) {
                    // Reset to default (PRESENT or REPOS)
                    dispo.setStatut(getDefaultStatusForDate(date));
                    dispo.setCongeLien(null);
                    dispo.setRaison(null);
                    disponibiliteRepository.save(dispo);
                }
            }
        }
    }

    @Override
    public boolean verifierConflits(Long employeId, LocalDate debut, LocalDate fin) {
        for (LocalDate date = debut; !date.isAfter(fin); date = date.plusDays(1)) {
            Optional<DisponibiliteEmploye> dispo = disponibiliteRepository.findByEmployeIdAndDate(employeId, date);
            if (dispo.isPresent() && dispo.get().getStatut() != StatutDisponibilite.PRESENT
                    && dispo.get().getStatut() != StatutDisponibilite.REPOS) {
                return true; // Already absent or has something else
            }
        }
        return false;
    }

    private StatutDisponibilite getDefaultStatusForDate(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        if (dayOfWeek == 6 || dayOfWeek == 7) {
            return StatutDisponibilite.REPOS;
        }
        return StatutDisponibilite.PRESENT;
    }
}
