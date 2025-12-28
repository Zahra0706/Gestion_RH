package com.example.gestion_rh.service;

import com.example.gestion_rh.entity.Annonce;
import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.repository.AnnonceRepository;
import com.example.gestion_rh.repository.CongeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StatistiqueService {

    private final EmployeService employeService;
    private final DepartementService departementService;
    private final CongeService congeService;
    private final CongeRepository congeRepository;
    private final AnnonceRepository annonceRepository;

    // ======================================
    // ✅ STATISTIQUES GÉNÉRALES
    // ======================================
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
        long totalConges = congeRepository.count(); // plus performant
        long congesEnAttente = congeService.getCongesByStatut(Conge.StatutConge.EN_ATTENTE_MANAGER).size()
                + congeService.getCongesByStatut(Conge.StatutConge.EN_ATTENTE_RH).size();

        long congesValides = congeService.getCongesByStatut(Conge.StatutConge.VALIDE_RH).size();

        // Calcul taux validation
        double tauxValidation = totalConges > 0 ? (congesValides * 100.0 / totalConges) : 0;

        // Départements
        long totalDepartements = departementService.getAllDepartements().size();
        long departementsAvecManager = departementService.getAllDepartements().stream()
                .filter(d -> d.getManager() != null)
                .count();

        // Taux activité
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

        // ✅ correction division par zéro
        stats.put("tauxCouvertureManagers",
                totalDepartements > 0 ? (departementsAvecManager * 100.0 / totalDepartements) : 0);

        return stats;
    }

    // ======================================
    // ✅ STATISTIQUES PAR ROLE
    // ======================================
    public Map<String, Object> getStatistiquesParRole() {
        Map<String, Object> stats = new HashMap<>();

        employeService.getAllEmployes().forEach(employe -> {
            if (employe.getRole() != null) {
                String role = employe.getRole().name();
                stats.put(role, (Long) stats.getOrDefault(role, 0L) + 1);
            }
        });

        return stats;
    }

    // ======================================
    // ✅ EVOLUTION CONGÉS PAR MOIS
    // ======================================
    public Map<String, Object> getEvolutionCongesParMois(int annee) {
        Map<String, Object> result = new HashMap<>();

        String[] moisLabels = { "Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc" };
        int[] congesParMois = new int[12];

        List<Conge> congesAnnee = congeRepository.findAll().stream()
                .filter(c -> c.getDateDebut() != null && c.getDateDebut().getYear() == annee)
                .collect(Collectors.toList());

        for (Conge conge : congesAnnee) {
            int mois = conge.getDateDebut().getMonthValue() - 1;
            if (mois >= 0 && mois < 12) {
                congesParMois[mois]++;
            }
        }

        result.put("labels", Arrays.asList(moisLabels));
        result.put("data", Arrays.stream(congesParMois).boxed().collect(Collectors.toList()));

        return result;
    }

    // ======================================
    // ✅ EVOLUTION DETAILLEE CONGÉS
    // ======================================
    public Map<String, Object> getEvolutionCongesDetaillee(int annee) {
        Map<String, Object> result = new HashMap<>();

        String[] moisLabels = { "Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc" };
        int[] enAttenteParMois = new int[12];
        int[] validesParMois = new int[12];
        int[] refusParMois = new int[12];

        List<Conge> congesAnnee = congeRepository.findAll().stream()
                .filter(c -> c.getDateDebut() != null && c.getDateDebut().getYear() == annee)
                .collect(Collectors.toList());

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

    // ======================================
    // ✅ STATISTIQUES FILTRÉES
    // ======================================
    public Map<String, Object> getStatistiquesFiltrees(LocalDate debut, LocalDate fin,
                                                       Long departementId,
                                                       Annonce.TypeAnnonce typeAnnonce,
                                                       Boolean statutEmploye) {

        Map<String, Object> stats = new HashMap<>();

        // 1️⃣ Filtrer les employés
        List<com.example.gestion_rh.entity.Employe> employes = employeService.getAllEmployes().stream()
                .filter(e -> departementId == null ||
                        (e.getDepartement() != null && e.getDepartement().getId().equals(departementId)))
                .filter(e -> statutEmploye == null ||
                        (e.getIsCompteActif() != null && e.getIsCompteActif().equals(statutEmploye)))
                .collect(Collectors.toList());

        long totalEmployes = employes.size();
        long employesActifs = employes.stream().filter(e -> Boolean.TRUE.equals(e.getIsCompteActif())).count();
        long employesInactifs = totalEmployes - employesActifs;
        double tauxActivite = totalEmployes > 0 ? (employesActifs * 100.0 / totalEmployes) : 0;

        stats.put("totalEmployes", totalEmployes);
        stats.put("employesActifs", employesActifs);
        stats.put("employesInactifs", employesInactifs);
        stats.put("tauxActivite", String.format("%.1f", tauxActivite));

        // 2️⃣ Filtrer les congés
        List<Conge> conges = congeRepository.findAll().stream()
                .filter(c -> (debut == null || !c.getDateDebut().isBefore(debut)))
                .filter(c -> (fin == null || !c.getDateDebut().isAfter(fin)))
                .filter(c -> departementId == null ||
                        (c.getEmploye().getDepartement() != null &&
                                c.getEmploye().getDepartement().getId().equals(departementId)))
                .collect(Collectors.toList());

        long totalConges = conges.size();
        long congesEnAttente = conges.stream()
                .filter(c -> c.getStatut() == Conge.StatutConge.EN_ATTENTE_MANAGER ||
                        c.getStatut() == Conge.StatutConge.EN_ATTENTE_RH)
                .count();
        long congesValides = conges.stream()
                .filter(c -> c.getStatut() == Conge.StatutConge.VALIDE_RH)
                .count();

        double tauxValidation = totalConges > 0 ? (congesValides * 100.0 / totalConges) : 0;

        stats.put("totalConges", totalConges);
        stats.put("congesEnAttente", congesEnAttente);
        stats.put("congesValides", congesValides);
        stats.put("tauxValidation", String.format("%.1f", tauxValidation));

        // 3️⃣ Départements
        List<com.example.gestion_rh.entity.Departement> departements = departementService.getAllDepartements().stream()
                .filter(d -> departementId == null || d.getId().equals(departementId))
                .collect(Collectors.toList());

        long totalDepartements = departements.size();
        long departementsAvecManager = departements.stream().filter(d -> d.getManager() != null).count();
        double tauxCouvertureManagers = totalDepartements > 0
                ? (departementsAvecManager * 100.0 / totalDepartements) : 0;

        stats.put("totalDepartements", totalDepartements);
        stats.put("departementsAvecManager", departementsAvecManager);
        stats.put("departementsSansManager", totalDepartements - departementsAvecManager);
        stats.put("tauxCouvertureManagers", String.format("%.1f", tauxCouvertureManagers));

        // 4️⃣ Filtrer les annonces
        List<com.example.gestion_rh.entity.Annonce> annonces = annonceRepository.findAll().stream()
                .filter(a -> debut == null || !a.getDatePublication().toLocalDate().isBefore(debut))
                .filter(a -> fin == null || !a.getDatePublication().toLocalDate().isAfter(fin))
                .filter(a -> typeAnnonce == null || a.getType().equals(typeAnnonce))
                .filter(a -> departementId == null ||
                        a.getDepartement() == null ||
                        a.getDepartement().getId().equals(departementId))
                .collect(Collectors.toList());

        stats.put("totalAnnonces", annonces.size());

        return stats;
    }

    // ======================================
    // ✅ EXPORT DATA
    // ======================================
    public Map<String, Object> getExportData(LocalDate debut, LocalDate fin,
                                             Long departementId,
                                             Annonce.TypeAnnonce typeAnnonce,
                                             Boolean statutEmploye) {

        Map<String, Object> stats = getStatistiquesFiltrees(debut, fin, departementId, typeAnnonce, statutEmploye);

        // ✅ simplification : liste propre
        List<String> analysis = new ArrayList<>();

        stats.put("analysis", analysis);
        return stats;
    }
}
