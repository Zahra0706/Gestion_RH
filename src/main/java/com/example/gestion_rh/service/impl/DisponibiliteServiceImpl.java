package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.DisponibiliteEmploye;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.StatutDisponibilite;
import com.example.gestion_rh.repository.DisponibiliteRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.DisponibiliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DisponibiliteServiceImpl implements DisponibiliteService {

    private final DisponibiliteRepository disponibiliteRepository;
    private final EmployeRepository employeRepository;

    @Override
    public List<DisponibiliteEmploye> getDisponibilites(LocalDate date) {
        // Ensure all active employees have an entry for this date
        synchroniserEmployesPourDate(date);
        return disponibiliteRepository.findByDate(date);
    }

    @Override
    public List<DisponibiliteEmploye> getDisponibilitesParDepartement(Long departementId, LocalDate date) {
        synchroniserEmployesPourDate(date);
        return disponibiliteRepository.findByEmployeDepartementIdAndDate(departementId, date);
    }

    private void synchroniserEmployesPourDate(LocalDate date) {
        List<Employe> employesActifs = employeRepository.findAll().stream()
                .filter(Employe::getIsCompteActif)
                .collect(Collectors.toList());

        for (Employe employe : employesActifs) {
            disponibiliteRepository.findByEmployeIdAndDate(employe.getId(), date)
                    .orElseGet(() -> {
                        DisponibiliteEmploye dispo = DisponibiliteEmploye.builder()
                                .employe(employe)
                                .date(date)
                                .statut(getDefaultStatusForDate(date))
                                .estPlanifie(true)
                                .build();
                        return disponibiliteRepository.save(dispo);
                    });
        }
    }

    private StatutDisponibilite getDefaultStatusForDate(LocalDate date) {
        // Weekends as REPOS
        int dayOfWeek = date.getDayOfWeek().getValue();
        if (dayOfWeek == 6 || dayOfWeek == 7) {
            return StatutDisponibilite.REPOS;
        }
        return StatutDisponibilite.PRESENT;
    }

    @Override
    public DisponibiliteEmploye modifierStatut(Long employeId, LocalDate date, StatutDisponibilite statut,
            String raison, String commentaire, Long modifieParId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
        Employe modificateur = employeRepository.findById(modifieParId)
                .orElse(null);

        DisponibiliteEmploye dispo = disponibiliteRepository.findByEmployeIdAndDate(employeId, date)
                .orElse(DisponibiliteEmploye.builder()
                        .employe(employe)
                        .date(date)
                        .build());

        dispo.setStatut(statut);
        dispo.setRaison(raison);
        dispo.setCommentaire(commentaire);
        dispo.setModifiePar(modificateur);
        dispo.setEstPlanifie(false); // Manual override is not a simple "planified" rule
        dispo.setDateModification(LocalDateTime.now());

        return disponibiliteRepository.save(dispo);
    }

    @Override
    public void appliquerStatutPeriode(Long employeId, LocalDate debut, LocalDate fin, StatutDisponibilite statut,
            String raison, Long modifieParId) {
        for (LocalDate date = debut; !date.isAfter(fin); date = date.plusDays(1)) {
            modifierStatut(employeId, date, statut, raison, "Application sur période", modifieParId);
        }
    }

    @Override
    public Map<String, Object> getStatistiquesPresence(LocalDate date) {
        List<DisponibiliteEmploye> dispos = getDisponibilites(date);
        Map<String, Object> stats = new HashMap<>();

        long total = dispos.size();
        Map<StatutDisponibilite, Long> counts = dispos.stream()
                .collect(Collectors.groupingBy(DisponibiliteEmploye::getStatut, Collectors.counting()));

        stats.put("total", total);
        stats.put("counts", counts);
        stats.put("presencePercent",
                total > 0 ? (counts.getOrDefault(StatutDisponibilite.PRESENT, 0L) * 100.0 / total) : 0);

        return stats;
    }

    @Override
    public void traiterReglesAutomatiques(LocalDate date) {
        // Implement auto-rules logic here if needed (e.g. holidays)
    }
}
