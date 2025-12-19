package com.example.gestion_rh.service;

import com.example.gestion_rh.repository.CongeRepository;
import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Annonce;
import com.example.gestion_rh.repository.AnnonceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // Added missing import
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
    private final AnnonceRepository annonceRepository;

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
        String[] moisLabels = { "Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc" };
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

        String[] moisLabels = { "Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc" };
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
    // --- STATISTIQUES FILTRÉES ---

    public Map<String, Object> getStatistiquesFiltrees(LocalDate debut, LocalDate fin,
            Long departementId,
            Annonce.TypeAnnonce typeAnnonce,
            Boolean statutEmploye) {
        Map<String, Object> stats = new HashMap<>();

        // 1. Filtrer les employés
        List<com.example.gestion_rh.entity.Employe> employes = employeService.getAllEmployes().stream()
                .filter(e -> departementId == null
                        || (e.getDepartement() != null && e.getDepartement().getId().equals(departementId)))
                .filter(e -> statutEmploye == null
                        || (e.getIsCompteActif() != null && e.getIsCompteActif().equals(statutEmploye)))
                .collect(Collectors.toList());

        long totalEmployes = employes.size();
        long employesActifs = employes.stream().filter(e -> Boolean.TRUE.equals(e.getIsCompteActif())).count();
        long employesInactifs = totalEmployes - employesActifs;
        double tauxActivite = totalEmployes > 0 ? (employesActifs * 100.0 / totalEmployes) : 0;

        stats.put("totalEmployes", totalEmployes);
        stats.put("employesActifs", employesActifs);
        stats.put("employesInactifs", employesInactifs);
        stats.put("tauxActivite", String.format("%.1f", tauxActivite));

        // 2. Filtrer les congés
        List<Conge> conges = congeRepository.findAll().stream()
                .filter(c -> {
                    // Filtre par date (si spécifié) : on regarde si le congé commence dans la
                    // période
                    if (debut != null && c.getDateDebut().isBefore(debut))
                        return false;
                    if (fin != null && c.getDateDebut().isAfter(fin))
                        return false;
                    return true;
                })
                .filter(c -> {
                    // Filtre par département (via l'employé)
                    if (departementId != null) {
                        return c.getEmploye().getDepartement() != null &&
                                c.getEmploye().getDepartement().getId().equals(departementId);
                    }
                    return true;
                })
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

        List<com.example.gestion_rh.entity.Departement> departements = departementService.getAllDepartements().stream()
                .filter(d -> departementId == null || d.getId().equals(departementId))
                .collect(Collectors.toList());

        long totalDepartements = departements.size();
        long departementsAvecManager = departements.stream().filter(d -> d.getManager() != null).count();
        double tauxCouvertureManagers = totalDepartements > 0 ? (departementsAvecManager * 100.0 / totalDepartements)
                : 0;

        stats.put("totalDepartements", totalDepartements);
        stats.put("departementsAvecManager", departementsAvecManager);
        stats.put("departementsSansManager", totalDepartements - departementsAvecManager);
        stats.put("tauxCouvertureManagers", String.format("%.1f", tauxCouvertureManagers));

        // 4. Filtrer les Annonces
        List<com.example.gestion_rh.entity.Annonce> annonces = annonceRepository.findAll().stream()
                .filter(a -> {
                    if (debut != null && a.getDatePublication().toLocalDate().isBefore(debut))
                        return false;
                    if (fin != null && a.getDatePublication().toLocalDate().isAfter(fin))
                        return false;
                    return true;
                })
                .filter(a -> {
                    if (typeAnnonce != null && !a.getType().equals(typeAnnonce))
                        return false;
                    return true;
                })
                .filter(a -> {
                    if (departementId != null) {
                        return a.getDepartement() == null || a.getDepartement().getId().equals(departementId);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        long totalAnnonces = annonces.size();
        long generalesCount = annonces.stream()
                .filter(a -> a.getType() == com.example.gestion_rh.entity.Annonce.TypeAnnonce.GENERALE).count();
        long importantesCount = annonces.stream()
                .filter(a -> a.getType() == com.example.gestion_rh.entity.Annonce.TypeAnnonce.IMPORTANTE).count();
        long departementCount = annonces.stream()
                .filter(a -> a.getType() == com.example.gestion_rh.entity.Annonce.TypeAnnonce.DEPARTEMENT).count();
        long informationCount = annonces.stream()
                .filter(a -> a.getType() == com.example.gestion_rh.entity.Annonce.TypeAnnonce.INFORMATION).count();

        stats.put("totalAnnonces", totalAnnonces);
        stats.put("generalesCount", generalesCount);
        stats.put("importantesCount", importantesCount);
        stats.put("departementCount", departementCount);
        stats.put("informationCount", informationCount);

        // --- CHART DATA RECALCULATION (Simplified for now) ---
        // Recalculate Role Distribution based on filtered employees
        Map<String, Long> roleStats = new HashMap<>();
        employes.forEach(e -> {
            String role = e.getRole().name();
            roleStats.put(role, roleStats.getOrDefault(role, 0L) + 1);
        });
        stats.put("statistiquesParRole", roleStats);

        // Recalculate Dept Distribution based on filtered employees
        Map<String, Long> deptStats = new HashMap<>();
        // If specific dept selected, this will have 1 entry. If not, multiple.
        if (departementId != null) {
            // Only current dept
            long count = employes.size(); // All filtered employees are in this dept
            if (!employes.isEmpty()) {
                deptStats.put(employes.get(0).getDepartement().getNom(), count);
            }
        } else {
            // Group by dept
            employes.forEach(e -> {
                if (e.getDepartement() != null) {
                    String dName = e.getDepartement().getNom();
                    deptStats.put(dName, deptStats.getOrDefault(dName, 0L) + 1);
                }
            });
        }
        stats.put("employesParDepartement", deptStats);

        return stats;
    }

    public Map<String, Object> getExportData(LocalDate debut, LocalDate fin,
            Long departementId,
            Annonce.TypeAnnonce typeAnnonce,
            Boolean statutEmploye) {

        // 1. Récupérer les statistiques de base filtrées
        Map<String, Object> stats = getStatistiquesFiltrees(debut, fin, departementId, typeAnnonce, statutEmploye);

        // 2. Ajouter les métadonnées des filtres
        Map<String, Object> filters = new HashMap<>();
        filters.put("dateDebut", debut);
        filters.put("dateFin", fin);
        filters.put("departementId", departementId);
        if (departementId != null) {
            departementService.getDepartementById(departementId)
                    .ifPresent(d -> filters.put("departementNom", d.getNom()));
        }
        filters.put("typeAnnonce", typeAnnonce);
        filters.put("statutEmploye", statutEmploye);
        stats.put("filters", filters);

        // 3. Générer l'analyse (Insights)
        List<String> analysis = java.util.ArrayList.class.isAssignableFrom(List.class) ? new java.util.ArrayList<>()
                : new java.util.ArrayList<>();

        long totalEmp = (long) stats.get("totalEmployes");
        if (totalEmp > 0) {
            String tauxActiviteStr = (String) stats.get("tauxActivite");
            try {
                double tauxActivite = Double.parseDouble(tauxActiviteStr.replace(",", "."));
                if (tauxActivite >= 80) {
                    analysis.add("Performance RH: Excellent taux d'activité des employés (" + tauxActiviteStr + "%).");
                } else if (tauxActivite < 50) {
                    analysis.add("Attention: Taux d'activité critique (<50%). Vérifier les départs ou désactivations.");
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        long totalConges = (long) stats.get("totalConges");
        if (totalConges > 0) {
            String tauxValidationStr = (String) stats.get("tauxValidation");
            try {
                double tauxValidation = Double.parseDouble(tauxValidationStr.replace(",", "."));
                if (tauxValidation > 90)
                    analysis.add("Gestion Congés: Taux de validation très élevé.");
                if (tauxValidation < 30)
                    analysis.add("Gestion Congés: Taux de validation faible, potentiel goulot d'étranglement.");
            } catch (NumberFormatException e) {
            }
        }

        stats.put("analysis", analysis);

        // 4. Détails des Départements (Annexes)
        // On récupère tous les départements (ou celui filtré)
        List<com.example.gestion_rh.entity.Departement> depts = departementService.getAllDepartements().stream()
                .filter(d -> departementId == null || d.getId().equals(departementId))
                .collect(Collectors.toList());

        List<Map<String, Object>> deptDetails = depts.stream().map(d -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("nom", d.getNom());
            detail.put("manager", d.getManager() != null ? d.getManager().getPrenom() + " " + d.getManager().getNom()
                    : "Non assigné");
            detail.put("totalEmployes", d.getEmployes() != null ? d.getEmployes().size() : 0);

            // Calculer les actifs dans ce département (snapshot actuel)
            long actifs = d.getEmployes() != null
                    ? d.getEmployes().stream().filter(e -> Boolean.TRUE.equals(e.getIsCompteActif())).count()
                    : 0;
            detail.put("employesActifs", actifs);
            detail.put("tauxOccupation",
                    (d.getEmployes() != null && !d.getEmployes().isEmpty())
                            ? String.format("%.0f%%", (double) actifs * 100 / d.getEmployes().size())
                            : "0%");

            return detail;
        }).collect(Collectors.toList());

        stats.put("departmentDetails", deptDetails);

        return stats;
    }
}