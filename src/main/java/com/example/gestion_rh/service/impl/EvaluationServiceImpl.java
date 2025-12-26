package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.dto.EvaluationDTO;
import com.example.gestion_rh.entity.*;
import com.example.gestion_rh.repository.*;
import com.example.gestion_rh.service.EvaluationService;
import com.example.gestion_rh.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationCampagneRepository campagneRepository;
    private final EmployeRepository employeRepository;
    private final EmailService emailService;

    @Override
    public EvaluationCampagne creerCampagne(EvaluationCampagne campagne, Long rhId) {
        Employe rh = employeRepository.findById(rhId)
                .orElseThrow(() -> new RuntimeException("RH not found"));

        campagne.setCreateurRH(rh);
        campagne.setStatut(StatutCampagne.PLANIFIEE);

        return campagneRepository.save(campagne);
    }

    @Override
    public List<Evaluation> genererEvaluationsPourCampagne(Long campagneId) {
        EvaluationCampagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne not found"));

        // Get all active employees
        List<Employe> employes = employeRepository.findAll().stream()
                .filter(e -> e.getIsCompteActif() != null && e.getIsCompteActif()) // Active only
                .collect(Collectors.toList());

        System.out.println("=== DEBUG: Génération d'évaluations ===");
        System.out.println("Nombre total d'employés: " + employeRepository.findAll().size());
        System.out.println("Nombre d'employés actifs: " + employes.size());

        List<Evaluation> evaluations = new ArrayList<>();

        for (Employe employe : employes) {
            // Check if evaluation already exists
            Optional<Evaluation> existing = evaluationRepository
                    .findByEmployeIdAndCampagneId(employe.getId(), campagneId);

            if (existing.isEmpty()) {
                Evaluation evaluation = new Evaluation();
                evaluation.setEmploye(employe);
                evaluation.setCampagne(campagne);
                evaluation.setAnnee(campagne.getAnnee()); // Set year from campaign
                evaluation.setCreateurRH(campagne.getCreateurRH());
                // Manager will be set manually or based on department hierarchy
                evaluation.setManager(null);
                evaluation.setDateCreation(LocalDate.now());
                evaluation.setStatut(StatutEvaluation.EN_ATTENTE);
                evaluation.setBesoinFormation(false);

                // Initialize scores to 0.0 to satisfy DB constraints
                evaluation.setScorePerformance(0.0);
                evaluation.setScoreCompetences(0.0);
                evaluation.setScoreObjectifs(0.0);
                evaluation.setScoreGlobal(0.0);

                evaluations.add(evaluationRepository.save(evaluation));
                System.out.println("Évaluation créée pour: " + employe.getNom() + " " + employe.getPrenom());
            } else {
                System.out.println("Évaluation déjà existante pour: " + employe.getNom() + " " + employe.getPrenom());
            }
        }

        System.out.println("Total évaluations créées: " + evaluations.size());

        // Update campaign status
        campagne.setStatut(StatutCampagne.EN_COURS);
        campagneRepository.save(campagne);

        return evaluations;
    }

    @Override
    public void envoyerEvaluationsAuxManagers(Long campagneId) {
        List<Evaluation> evaluations = evaluationRepository.findByCampagneId(campagneId);

        // Group by manager
        Map<Employe, List<Evaluation>> evaluationsByManager = evaluations.stream()
                .filter(e -> e.getManager() != null)
                .collect(Collectors.groupingBy(Evaluation::getManager));

        // Send email to each manager
        for (Map.Entry<Employe, List<Evaluation>> entry : evaluationsByManager.entrySet()) {
            Employe manager = entry.getKey();
            List<Evaluation> managerEvaluations = entry.getValue();

            // Mock email sending - implement with EmailService
            try {
                emailService.envoyerEmail(
                        manager.getEmail(),
                        "Évaluations à compléter",
                        "Vous avez " + managerEvaluations.size() + " évaluations à compléter.");
            } catch (Exception e) {
                // Log error
            }
        }
    }

    @Override
    public Evaluation sauvegarderEvaluation(Long evaluationId, EvaluationDTO dto) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        // Update scores
        evaluation.setScorePerformance(dto.getScorePerformance());
        evaluation.setScoreCompetences(dto.getScoreCompetences());
        evaluation.setScoreObjectifs(dto.getScoreObjectifs());

        // Calculate global score
        evaluation.setScoreGlobal(calculerScoreGlobal(evaluation));

        // Update comments
        if (dto.getCommentairesManager() != null) {
            evaluation.setCommentairesManager(dto.getCommentairesManager());
        }
        if (dto.getCommentairesRH() != null) {
            evaluation.setCommentairesRH(dto.getCommentairesRH());
        }

        evaluation.setPointsForts(dto.getPointsForts());
        evaluation.setAxesAmelioration(dto.getAxesAmelioration());
        evaluation.setPlanAction(dto.getPlanAction());
        evaluation.setBesoinFormation(dto.getBesoinFormation());

        // Update status and dates
        if (dto.getStatut() != null) {
            evaluation.setStatut(dto.getStatut());
        }
        if (dto.getDateEvaluation() != null) {
            evaluation.setDateEvaluation(dto.getDateEvaluation());
        }

        if (evaluation.getStatut() == StatutEvaluation.COMPLETEE) {
            evaluation.setDateCompletion(LocalDate.now());
        }

        return evaluationRepository.save(evaluation);
    }

    @Override
    public Evaluation getEvaluationById(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));
    }

    @Override
    public List<Evaluation> getEvaluationsByManager(Long managerId) {
        return evaluationRepository.findByManagerId(managerId);
    }

    @Override
    public List<Evaluation> getEvaluationsByCampagne(Long campagneId) {
        return evaluationRepository.findByCampagneId(campagneId);
    }

    @Override
    public List<Evaluation> getEvaluationsByEmploye(Long employeId) {
        return evaluationRepository.findByEmployeId(employeId);
    }

    @Override
    public Map<String, Object> calculerStatistiquesCampagne(Long campagneId) {
        List<Evaluation> evaluations = evaluationRepository.findByCampagneId(campagneId);

        Map<String, Object> stats = new HashMap<>();

        stats.put("total", evaluations.size());
        stats.put("completees", evaluations.stream()
                .filter(e -> e.getStatut() == StatutEvaluation.COMPLETEE).count());
        stats.put("enCours", evaluations.stream()
                .filter(e -> e.getStatut() == StatutEvaluation.EN_COURS).count());
        stats.put("enAttente", evaluations.stream()
                .filter(e -> e.getStatut() == StatutEvaluation.EN_ATTENTE).count());
        stats.put("enRetard", evaluations.stream()
                .filter(e -> e.getStatut() == StatutEvaluation.RETARD).count());

        // Calculate average scores
        List<Evaluation> completedEvals = evaluations.stream()
                .filter(e -> e.getScoreGlobal() != null)
                .collect(Collectors.toList());

        if (!completedEvals.isEmpty()) {
            double avgGlobal = completedEvals.stream()
                    .mapToDouble(Evaluation::getScoreGlobal)
                    .average()
                    .orElse(0.0);
            stats.put("scoreGlobalMoyen", avgGlobal);
        }

        return stats;
    }

    @Override
    public List<Evaluation> getEvaluationsEnRetard() {
        return evaluationRepository.findByStatut(StatutEvaluation.RETARD);
    }

    @Override
    public void envoyerRappelManagers(List<Long> managerIds) {
        for (Long managerId : managerIds) {
            List<Evaluation> pending = evaluationRepository.findByManagerId(managerId).stream()
                    .filter(e -> e.getStatut() != StatutEvaluation.COMPLETEE)
                    .collect(Collectors.toList());

            if (!pending.isEmpty()) {
                Employe manager = employeRepository.findById(managerId).orElse(null);
                if (manager != null) {
                    try {
                        emailService.envoyerEmail(
                                manager.getEmail(),
                                "Rappel: Évaluations en attente",
                                "Vous avez " + pending.size() + " évaluations en attente de complétion.");
                    } catch (Exception e) {
                        // Log error
                    }
                }
            }
        }
    }

    @Override
    public Double calculerScoreGlobal(Evaluation evaluation) {
        if (evaluation.getScorePerformance() == null ||
                evaluation.getScoreCompetences() == null ||
                evaluation.getScoreObjectifs() == null) {
            return null;
        }

        return (evaluation.getScorePerformance() * 0.4) +
                (evaluation.getScoreCompetences() * 0.3) +
                (evaluation.getScoreObjectifs() * 0.3);
    }

    @Override
    public String getCategoriePerformance(Double score) {
        if (score == null)
            return "NON_EVALUE";
        if (score >= 4.5)
            return "EXCELLENT";
        if (score >= 4.0)
            return "TRES_BIEN";
        if (score >= 3.0)
            return "SATISFAISANT";
        if (score >= 2.0)
            return "A_AMELIORER";
        return "INSUFFISANT";
    }

    @Override
    public Map<String, Long> getRepartitionParCategorie(Long campagneId) {
        List<Evaluation> evaluations = evaluationRepository.findByCampagneId(campagneId).stream()
                .filter(e -> e.getScoreGlobal() != null)
                .collect(Collectors.toList());

        return evaluations.stream()
                .collect(Collectors.groupingBy(
                        e -> getCategoriePerformance(e.getScoreGlobal()),
                        Collectors.counting()));
    }

    @Override
    public void assignerManager(Long evaluationId, Long managerId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        Employe manager = employeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        evaluation.setManager(manager);
        evaluationRepository.save(evaluation);
    }

    @Override
    public Evaluation saveEvaluation(Evaluation evaluation) {
        return evaluationRepository.save(evaluation);
    }

    @Override
    public Double calculerScoreGlobal(Double scorePerformance, Double scoreCompetences, Double scoreObjectifs) {
        if (scorePerformance == null || scoreCompetences == null || scoreObjectifs == null) {
            return null;
        }
        // 40% Performance + 30% Competences + 30% Objectifs
        return (scorePerformance * 0.4) + (scoreCompetences * 0.3) + (scoreObjectifs * 0.3);
    }

    @Override
    public List<Evaluation> genererEvaluationsGroupees(Long campagneId, Long managerId, List<Long> employeIds) {
        EvaluationCampagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne not found"));

        Employe manager = employeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        List<Evaluation> results = new ArrayList<>();
        for (Long empId : employeIds) {
            Employe employe = employeRepository.findById(empId)
                    .orElseThrow(() -> new RuntimeException("Employe not found: " + empId));

            // Logic to avoid duplicates but allowing manager update if null
            Optional<Evaluation> existing = evaluationRepository.findByEmployeIdAndCampagneId(empId, campagneId);
            if (existing.isEmpty()) {
                Evaluation evaluation = new Evaluation();
                evaluation.setEmploye(employe);
                evaluation.setManager(manager);
                evaluation.setCampagne(campagne);
                evaluation.setAnnee(campagne.getAnnee());
                evaluation.setCreateurRH(campagne.getCreateurRH());
                evaluation.setDateCreation(LocalDate.now());
                evaluation.setStatut(StatutEvaluation.EN_ATTENTE);

                // Initialize scores to 0.0 to satisfy DB constraints
                evaluation.setScorePerformance(0.0);
                evaluation.setScoreCompetences(0.0);
                evaluation.setScoreObjectifs(0.0);
                evaluation.setScoreGlobal(0.0);
                results.add(evaluationRepository.save(evaluation));
            } else {
                Evaluation eval = existing.get();
                if (eval.getManager() == null) {
                    eval.setManager(manager);
                    evaluationRepository.save(eval);
                    results.add(eval);
                }
            }
        }

        if (campagne.getStatut() == StatutCampagne.PLANIFIEE && !results.isEmpty()) {
            campagne.setStatut(StatutCampagne.EN_COURS);
            campagneRepository.save(campagne);
        }

        return results;
    }

    @Override
    public void supprimerEvaluation(Long id) {
        evaluationRepository.deleteById(id);
    }
}
