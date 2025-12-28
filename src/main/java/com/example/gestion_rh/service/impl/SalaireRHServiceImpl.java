package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.dto.SalaireDTO;
import com.example.gestion_rh.entity.Contrat;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.entity.Salaire;
import com.example.gestion_rh.repository.ContratRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.repository.SalaireRepository;
import com.example.gestion_rh.service.SalaireRHService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaireRHServiceImpl implements SalaireRHService {

    private final SalaireRepository salaireRepository;
    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;

    private static final double TAUX_COTISATIONS_DEFAULT = 0.22; // 22%
    private static final double TAUX_IMPOTS_DEFAULT = 0.15; // 15%
    private static final double PRIME_ANCIENNETE_PAR_AN = 0.01; // 1% per year

    @Override
    @Transactional
    public Salaire calculerSalaireAutomatique(Long employeId, String moisAnnee) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé"));

        // Find active contract for salary base
        Contrat contrat = contratRepository.findByEmployeId(employeId).stream()
                .filter(c -> c.getStatut() != Contrat.StatutContrat.ARCHIVE) // Simple filter, improve as needed
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Aucun contrat actif trouvé pour cet employé"));

        double salaireBase = contrat.getSalaireBase();
        double anciennete = calculerPrimeAnciennete(employe);
        double cotisations = calculerCotisations(salaireBase);
        double brutImposable = salaireBase + anciennete - cotisations; // Simplification
        double impots = brutImposable * TAUX_IMPOTS_DEFAULT;

        double net = salaireBase + anciennete - cotisations - impots;

        // Check if salary already exists for this employee and month
        Salaire existingSalaire = salaireRepository.findByEmployeIdAndMoisAnnee(employeId, moisAnnee).orElse(null);

        Salaire salaryToSave;

        if (existingSalaire != null) {
            // Update existing salary
            salaryToSave = existingSalaire;
            salaryToSave.setSalaireBrut(salaireBase);
            salaryToSave.setSalaireNet(net);
            salaryToSave.setPrimeAnciennete(anciennete);
            salaryToSave.setCotisationsSociales(cotisations);
            salaryToSave.setImpots(impots);
            salaryToSave.setStatutPaiement("CALCULE");
            // Don't change referencePaiement or ID
        } else {
            // Create new salary
            salaryToSave = Salaire.builder()
                    .employe(employe)
                    .moisAnnee(moisAnnee)
                    .salaireBrut(salaireBase)
                    .salaireNet(net)
                    .primeAnciennete(anciennete)
                    .cotisationsSociales(cotisations)
                    .impots(impots)
                    .heuresTravaillees(160.0) // Default standard hours
                    .statutPaiement("CALCULE")
                    .modePaiement("VIREMENT")
                    .referencePaiement("PAY-" + moisAnnee + "-" + employe.getId())
                    .build();
        }

        return salaireRepository.save(salaryToSave);
    }

    @Override
    public Double calculerHeuresSupplementaires(Employe employe, Double heuresSupplementaires) {
        // Implement logic based on hourly rate derivation
        // Assuming conversion from monthly salary: Salary / 160 * 1.5 (for example)
        return 0.0; // Todo: implement correct calculation
    }

    @Override
    public Double calculerCotisations(Double salaireBrut) {
        return salaireBrut * TAUX_COTISATIONS_DEFAULT;
    }

    @Override
    public Double calculerPrimeAnciennete(Employe employe) {
        if (employe.getDateEmbauche() == null)
            return 0.0;
        int years = Period.between(employe.getDateEmbauche(), LocalDate.now()).getYears();
        // Assuming we need base salary here, fetch contract again or pass as arg in
        // real impl
        // For now, simplifying to 0 or needing refactor to pass salary
        // Let's grab contract quickly
        Contrat contrat = contratRepository.findByEmployeId(employe.getId()).stream()
                .filter(c -> !c.getStatut().equals("ARCHIVE"))
                .findFirst()
                .orElse(null);

        if (contrat != null) {
            return contrat.getSalaireBase() * (PRIME_ANCIENNETE_PAR_AN * years);
        }
        return 0.0;
    }

    @Override
    @Transactional
    public Salaire creerSalaireManuel(SalaireDTO dto) {
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé"));

        // Find existing or create new to avoid duplicates
        Salaire salaire = salaireRepository.findByEmployeIdAndMoisAnnee(employe.getId(), dto.getMoisAnnee())
                .orElse(Salaire.builder()
                        .employe(employe)
                        .moisAnnee(dto.getMoisAnnee())
                        .referencePaiement("MANUAL-" + System.currentTimeMillis())
                        .statutPaiement("CALCULE")
                        .modePaiement("VIREMENT")
                        .heuresTravaillees(160.0)
                        .build());

        // Update fields from DTO
        if (dto.getSalaireBase() != null)
            salaire.setSalaireBrut(dto.getSalaireBase());
        if (dto.getSalaireNet() != null)
            salaire.setSalaireNet(dto.getSalaireNet());
        if (dto.getPrimes() != null)
            salaire.setPrimes(dto.getPrimes());
        else if (salaire.getId() == null)
            salaire.setPrimes(0.0); // Init if new

        if (dto.getPrimeAnciennete() != null)
            salaire.setPrimeAnciennete(dto.getPrimeAnciennete());
        if (dto.getHeuresSupplementaires() != null)
            salaire.setHeuresSupplementaires(dto.getHeuresSupplementaires());

        if (dto.getCotisationsSociales() != null)
            salaire.setCotisationsSociales(dto.getCotisationsSociales());
        if (dto.getImpots() != null)
            salaire.setImpots(dto.getImpots());

        return salaireRepository.save(salaire);
    }

    @Override
    @Transactional
    public Salaire modifierSalaire(Long salaireId, SalaireDTO modifications) {
        Salaire salaire = salaireRepository.findById(salaireId)
                .orElseThrow(() -> new EntityNotFoundException("Salaire non trouvé"));

        if (modifications.getHeuresTravaillees() != null)
            salaire.setHeuresTravaillees(modifications.getHeuresTravaillees());
        if (modifications.getSalaireNet() != null)
            salaire.setSalaireNet(modifications.getSalaireNet());
        // ... apply other mods

        return salaireRepository.save(salaire);
    }

    @Override
    @Transactional
    public Salaire validerSalaire(Long salaireId) {
        Salaire salaire = salaireRepository.findById(salaireId)
                .orElseThrow(() -> new EntityNotFoundException("Salaire non trouvé"));
        salaire.setStatutPaiement("VALIDE");
        return salaireRepository.save(salaire);
    }

    @Override
    @Transactional
    public Salaire marquerCommePaye(Long salaireId) {
        Salaire salaire = salaireRepository.findById(salaireId)
                .orElseThrow(() -> new EntityNotFoundException("Salaire non trouvé"));
        salaire.setStatutPaiement("PAYE");
        salaire.setDatePaiement(LocalDate.now());
        return salaireRepository.save(salaire);
    }

    @Override
    @Transactional
    public void deleteSalaire(Long id) {
        if (!salaireRepository.existsById(id)) {
            throw new EntityNotFoundException("Salaire non trouvé");
        }
        salaireRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> getStatistiques(String moisAnnee, String statut) {
        // Reuse the criteria logic to get the filtered list
        List<Salaire> salaires = getSalairesByCriteria(moisAnnee, statut);

        double totalMasseSalariale = salaires.stream().mapToDouble(Salaire::getSalaireBrut).sum();
        double totalNet = salaires.stream().mapToDouble(Salaire::getSalaireNet).sum();
        double moyenneSalaire = salaires.stream().mapToDouble(Salaire::getSalaireNet).average().orElse(0.0);
        long enAttente = salaires.stream().filter(s -> !"PAYE".equals(s.getStatutPaiement())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMasseSalariale", totalMasseSalariale);
        stats.put("totalNet", totalNet);
        stats.put("moyenneSalaire", moyenneSalaire);
        stats.put("enAttente", enAttente);
        stats.put("count", salaires.size());
        return stats;
    }

    @Override
    public Map<String, Double> getRepartitionSalairesParDepartement(String moisAnnee) {
        // Implement complex aggregation using repository or stream
        return new HashMap<>();
    }

    @Override
    public List<SalaireDTO> getEvolutionSalairesEmploye(Long employeId, int nbMois) {
        // Implementation
        return List.of();
    }

    @Override
    public Map<String, Double> getEvolutionMasseSalariale(int nbMois) {
        Map<String, Double> evolution = new java.util.LinkedHashMap<>();
        YearMonth current = YearMonth.now();

        for (int i = nbMois - 1; i >= 0; i--) {
            YearMonth targetMonth = current.minusMonths(i);
            String moisStr = targetMonth.toString();
            List<Salaire> salaires = salaireRepository.findByMoisAnnee(moisStr);
            double masse = salaires.stream().mapToDouble(Salaire::getSalaireBrut).sum();
            evolution.put(moisStr, masse);
        }
        return evolution;
    }

    @Override
    public List<Salaire> getSalairesByCriteria(String moisAnnee, String statut) {
        // If status is 'TOUS' or null/empty, just use date filter
        if (statut == null || statut.trim().isEmpty() || "TOUS".equals(statut)) {
            return getSalairesByMois(moisAnnee);
        }

        // If date is empty/null but status is present
        if (moisAnnee == null || moisAnnee.trim().isEmpty()) {
            return salaireRepository.findByStatutPaiement(statut);
        }

        return salaireRepository.findByMoisAnneeAndStatutPaiement(moisAnnee, statut);
    }

    @Override
    public Salaire getSalaireById(Long id) {
        return salaireRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Salaire non trouvé"));
    }

    public List<Salaire> getSalairesByMois(String moisAnnee) {
        if (moisAnnee == null || moisAnnee.trim().isEmpty()) {
            return salaireRepository.findAll();
        }
        return salaireRepository.findByMoisAnnee(moisAnnee);
    }

    @Override
    public void updateSalaire(Salaire salaire) {
        salaireRepository.save(salaire);
    }
}
