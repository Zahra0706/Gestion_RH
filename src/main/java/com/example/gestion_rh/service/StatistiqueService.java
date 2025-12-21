package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Conge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatistiqueService {

    private final EmployeService employeService;
    private final DepartementService departementService;
    private final CongeService congeService;

    public Map<String, Object> getStatistiquesGenerales() {

        Map<String, Object> stats = new HashMap<>();

        // =====================
        // EMPLOYÉS
        // =====================
        long totalEmployes = employeService.getAllEmployes().size();
        long employesActifs = employeService.getEmployesActifs().size();
        long employesInactifs = employeService.getEmployesInactifs().size();

        // Par département
        Map<String, Long> employesParDepartement = new HashMap<>();
        departementService.getAllDepartements().forEach(dept -> {
            long count = employeService.countEmployesByDepartement(dept.getId());
            employesParDepartement.put(dept.getNom(), count);
        });

        // =====================
        // CONGÉS (STATUTS VALIDES)
        // =====================
        long congesEnAttenteManager = congeService.getCongesByStatut(Conge.StatutConge.EN_ATTENTE_MANAGER).size();
        long congesValidesManager   = congeService.getCongesByStatut(Conge.StatutConge.VALIDE_MANAGER).size();
        long congesRefusesManager   = congeService.getCongesByStatut(Conge.StatutConge.REFUSE_MANAGER).size();

        long congesValidesRH        = congeService.getCongesByStatut(Conge.StatutConge.VALIDE_RH).size();
        long congesRefusesRH        = congeService.getCongesByStatut(Conge.StatutConge.REFUSE_RH).size();

        long congesAnnules          = congeService.getCongesByStatut(Conge.StatutConge.ANNULE).size();

        long totalConges = congesEnAttenteManager + congesValidesManager + congesRefusesManager
                + congesValidesRH + congesRefusesRH + congesAnnules;

        // =====================
        // DÉPARTEMENTS
        // =====================
        long totalDepartements = departementService.getAllDepartements().size();
        long departementsAvecManager = departementService.getAllDepartements().stream()
                .filter(d -> d.getManager() != null)
                .count();

        double tauxOccupation = totalDepartements == 0 ? 0.0 :
                (departementsAvecManager * 100.0 / totalDepartements);

        // =====================
        // PUT VALUES
        // =====================
        stats.put("totalEmployes", totalEmployes);
        stats.put("employesActifs", employesActifs);
        stats.put("employesInactifs", employesInactifs);
        stats.put("employesParDepartement", employesParDepartement);

        stats.put("totalConges", totalConges);
        stats.put("congesEnAttente", congesEnAttenteManager);
        stats.put("congesValides", congesValidesRH); // tu peux choisir VALIDE_MANAGER si tu veux
        stats.put("congesRefuses", congesRefusesManager + congesRefusesRH);
        stats.put("congesAnnules", congesAnnules);

        stats.put("totalDepartements", totalDepartements);
        stats.put("departementsAvecManager", departementsAvecManager);
        stats.put("tauxOccupation", tauxOccupation);

        return stats;
    }

    public Map<String, Object> getStatistiquesParRole() {

        Map<String, Object> stats = new HashMap<>();

        employeService.getAllEmployes().forEach(employe -> {
            if (employe.getRole() != null) {
                String role = employe.getRole().name();
                long current = (long) stats.getOrDefault(role, 0L);
                stats.put(role, current + 1);
            }
        });

        return stats;
    }
}
