package com.example.gestion_rh.service;

import com.example.gestion_rh.repository.CongeRepository;
import com.example.gestion_rh.entity.Conge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StatistiqueService {

    private final EmployeService employeService;
    private final DepartementService departementService;
    private final CongeService congeService;
    private final CongeRepository congeRepository;


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
        long totalConges = congeRepository.count(); // Utilisez directement le repository
        long congesEnAttente = congeService.getCongesByStatut(
                com.example.gestion_rh.entity.Conge.StatutConge.EN_ATTENTE_MANAGER).size() +
                congeService.getCongesByStatut(
                        com.example.gestion_rh.entity.Conge.StatutConge.EN_ATTENTE_RH).size();
        long congesValides = congeService.getCongesByStatut(
                com.example.gestion_rh.entity.Conge.StatutConge.VALIDE_RH).size();

        // Calculer le taux de validation
        double tauxValidation = totalConges > 0 ? (congesValides * 100.0 / totalConges) : 0;

        // Départements
        long totalDepartements = departementService.getAllDepartements().size();
        long departementsAvecManager = departementService.getAllDepartements().stream()
                .filter(d -> d.getManager() != null)
                .count();

        // Taux d'activité
        double tauxActivite = totalEmployes > 0 ? (employesActifs * 100.0 / totalEmployes) : 0;

        stats.put("totalEmployes", totalEmployes);
        stats.put("employesActifs", employesActifs);
        stats.put("employesInactifs", employesInactifs);
        stats.put("tauxActivite", String.format("%.1f", tauxActivite));
        stats.put("employesParDepartement", employesParDepartement);
        stats.put("totalConges", totalConges);
        stats.put("congesEnAttente", congesEnAttente);
        stats.put("congesValides", congesValides);
        stats.put("tauxValidation", String.format("%.1f", tauxValidation));
        stats.put("totalDepartements", totalDepartements);
        stats.put("departementsAvecManager", departementsAvecManager);
        stats.put("departementsSansManager", totalDepartements - departementsAvecManager);
        stats.put("tauxCouvertureManagers", departementsAvecManager * 100.0 / totalDepartements);

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
    public Map<String, Object> getEvolutionCongesParMois(int annee) {
        Map<String, Object> result = new HashMap<>();

        // Mois en français
        String[] moisLabels = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
        int[] congesParMois = new int[12];

        // Récupérer tous les congés de l'année
        List<Conge> congesAnnee = congeRepository.findAll().stream()
                .filter(c -> c.getDateDebut() != null && c.getDateDebut().getYear() == annee)
                .collect(Collectors.toList());

        // Compter par mois
        for (Conge conge : congesAnnee) {
            int mois = conge.getDateDebut().getMonthValue() - 1; // 0-11
            if (mois >= 0 && mois < 12) {
                congesParMois[mois]++;
            }
        }

        result.put("labels", Arrays.asList(moisLabels));
        result.put("data", Arrays.stream(congesParMois).boxed().collect(Collectors.toList()));

        return result;
    }

    // Alternative : avec données réelles par statut
    public Map<String, Object> getEvolutionCongesDetaillee(int annee) {
        Map<String, Object> result = new HashMap<>();

        String[] moisLabels = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
        int[] enAttenteParMois = new int[12];
        int[] validesParMois = new int[12];
        int[] refusParMois = new int[12];

        // Récupérer tous les congés de l'année
        List<Conge> congesAnnee = congeRepository.findAll().stream()
                .filter(c -> c.getDateDebut() != null && c.getDateDebut().getYear() == annee)
                .collect(Collectors.toList());

        // Compter par mois et par statut
        for (Conge conge : congesAnnee) {
            int mois = conge.getDateDebut().getMonthValue() - 1;
            if (mois >= 0 && mois < 12) {
                if (conge.getStatut() == Conge.StatutConge.EN_ATTENTE_MANAGER ||
                        conge.getStatut() == Conge.StatutConge.EN_ATTENTE_RH) {
                    enAttenteParMois[mois]++;
                } else if (conge.getStatut() == Conge.StatutConge.VALIDE_RH) {
                    validesParMois[mois]++;
                } else if (conge.getStatut() == Conge.StatutConge.REFUSE_MANAGER ||
                        conge.getStatut() == Conge.StatutConge.REFUSE_RH) {
                    refusParMois[mois]++;
                }
            }
        }

        result.put("labels", Arrays.asList(moisLabels));
        result.put("enAttente", Arrays.stream(enAttenteParMois).boxed().collect(Collectors.toList()));
        result.put("valides", Arrays.stream(validesParMois).boxed().collect(Collectors.toList()));
        result.put("refus", Arrays.stream(refusParMois).boxed().collect(Collectors.toList()));

        return result;
    }
}