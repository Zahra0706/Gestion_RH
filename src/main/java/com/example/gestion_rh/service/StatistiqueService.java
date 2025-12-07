package com.example.gestion_rh.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class StatistiqueService {

    private final EmployeService employeService;
    private final DepartementService departementService;
    private final CongeService congeService;

    public Map<String, Object> getStatistiquesGenerales() {
        Map<String, Object> stats = new HashMap<>();

        // Total employés
        long totalEmployes = employeService.getAllEmployes().size();
        long employesActifs = employeService.getEmployesActifs().size();
        long employesInactifs = employeService.getEmployesInactifs().size();

        // Par département
        Map<String, Long> employesParDepartement = new HashMap<>();
        departementService.getAllDepartements().forEach(dept -> {
            long count = employeService.countEmployesByDepartement(dept.getId());
            employesParDepartement.put(dept.getNom(), count);
        });

        // Congés
        long totalConges = congeService.getCongesByStatut(null).size();
        long congesEnAttente = congeService.getCongesByStatut(
                com.example.gestion_rh.entity.Conge.StatutConge.EN_ATTENTE_MANAGER).size() +
                congeService.getCongesByStatut(
                        com.example.gestion_rh.entity.Conge.StatutConge.EN_ATTENTE_RH).size();
        long congesValides = congeService.getCongesByStatut(
                com.example.gestion_rh.entity.Conge.StatutConge.VALIDE_RH).size();

        // Départements
        long totalDepartements = departementService.getAllDepartements().size();
        long departementsAvecManager = departementService.getAllDepartements().stream()
                .filter(d -> d.getManager() != null)
                .count();

        stats.put("totalEmployes", totalEmployes);
        stats.put("employesActifs", employesActifs);
        stats.put("employesInactifs", employesInactifs);
        stats.put("employesParDepartement", employesParDepartement);
        stats.put("totalConges", totalConges);
        stats.put("congesEnAttente", congesEnAttente);
        stats.put("congesValides", congesValides);
        stats.put("totalDepartements", totalDepartements);
        stats.put("departementsAvecManager", departementsAvecManager);
        stats.put("tauxOccupation", departementsAvecManager * 100.0 / totalDepartements);

        return stats;
    }

    public Map<String, Object> getStatistiquesParRole() {
        Map<String, Object> stats = new HashMap<>();

        employeService.getAllEmployes().forEach(employe -> {
            String role = employe.getRole().name();
            stats.put(role, (Long) stats.getOrDefault(role, 0L) + 1);
        });

        return stats;
    }
}