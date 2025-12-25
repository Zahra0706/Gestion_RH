package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.dto.CongeStatsDTO;
import com.example.gestion_rh.dto.CongeValidationDTO;
import com.example.gestion_rh.dto.CalendarEventDTO;
import com.example.gestion_rh.dto.RhAlertDTO;
import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.CongeRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.CongeService;
import com.example.gestion_rh.service.CongeDisponibiliteIntegrationService;
import com.example.gestion_rh.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CongeServiceImpl implements CongeService {

    private final CongeRepository congeRepository;
    private final EmployeRepository employeRepository;
    private final EmailService emailService;
    private final CongeDisponibiliteIntegrationService congeDisponibiliteIntegrationService;

    private static final int JOURS_CONGES_ANNUEL = 30;

    @Override
    public Conge demanderConge(Conge conge) {
        if (conge.getDateDebut().isAfter(conge.getDateFin())) {
            throw new RuntimeException("Date de début doit être avant date de fin");
        }

        long joursDemandes = ChronoUnit.DAYS.between(conge.getDateDebut(), conge.getDateFin()) + 1;
        double joursRestants = calculerJoursCongesRestants(conge.getEmploye().getId(), conge.getDateDebut().getYear());

        if (joursDemandes > joursRestants) {
            throw new RuntimeException("Jours de congés insuffisants");
        }

        conge.setStatut(Conge.StatutConge.EN_ATTENTE_MANAGER);
        if (conge.getPriorite() == null) {
            conge.setPriorite(Conge.Priorite.NORMAL);
        }

        Conge savedConge = congeRepository.save(conge);

        if (conge.getEmploye().getDepartement() != null &&
                conge.getEmploye().getDepartement().getManager() != null) {
            emailService.envoyerNotificationCongeManager(savedConge);
        }

        return savedConge;
    }

    @Override
    public Conge validerCongeManager(Long congeId, Long managerId, boolean validation, String commentaire) {
        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Demande de congé non trouvée"));

        Employe manager = employeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager non trouvé"));

        if (conge.getEmploye().getDepartement() != null &&
                conge.getEmploye().getDepartement().getManager() != null &&
                !conge.getEmploye().getDepartement().getManager().getId().equals(managerId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à valider cette demande");
        }

        if (validation) {
            conge.setStatut(Conge.StatutConge.VALIDE_MANAGER);
            conge.setManagerValidateur(manager);
            conge.setCommentaireManager(commentaire);
            emailService.envoyerNotificationCongeRH(conge);
        } else {
            conge.setStatut(Conge.StatutConge.REFUSE_MANAGER);
            conge.setManagerValidateur(manager);
            conge.setCommentaireManager(commentaire);
            emailService.envoyerNotificationRefusConge(conge, "manager");
        }

        conge.setDateTraitement(LocalDateTime.now());
        return congeRepository.save(conge);
    }

    @Override
    public Conge validerCongeRH(Long congeId, Long rhId, boolean validation, String commentaire) {
        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Demande de congé non trouvée"));

        Employe rh = employeRepository.findById(rhId)
                .orElseThrow(() -> new RuntimeException("RH non trouvé"));

        if (validation) {
            conge.setStatut(Conge.StatutConge.VALIDE_RH);
            conge.setRhValidateur(rh);
            conge.setCommentaireRH(commentaire);

            // Notification d'approbation
            emailService.envoyerNotificationConge(
                    conge.getEmploye(),
                    "Votre demande de congé a été approuvée",
                    "Votre demande de congé du " + conge.getDateDebut() + " au " + conge.getDateFin()
                            + " a été approuvée par les RH.");

            // Sync with Presence module
            congeDisponibiliteIntegrationService.synchroniserConge(conge);
        } else {
            conge.setStatut(Conge.StatutConge.REFUSE_RH);
            conge.setRhValidateur(rh);
            conge.setCommentaireRH(commentaire);
            emailService.envoyerNotificationRefusConge(conge, "RH");
        }

        conge.setDateTraitement(LocalDateTime.now());
        return congeRepository.save(conge);
    }

    @Override
    public Conge mettreEnAttenteRH(Long congeId, Long rhId, String commentaire) {
        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Demande de congé non trouvée"));
        Employe rh = employeRepository.findById(rhId)
                .orElseThrow(() -> new RuntimeException("RH non trouvé"));

        conge.setStatut(Conge.StatutConge.EN_ATTENTE_INFO);
        conge.setRhValidateur(rh);
        conge.setCommentaireRH(commentaire);
        conge.setDateTraitement(LocalDateTime.now());

        return congeRepository.save(conge);
    }

    @Override
    public Conge renvoyerAuManager(Long congeId, Long rhId, String commentaire) {
        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Demande de congé non trouvée"));
        Employe rh = employeRepository.findById(rhId)
                .orElseThrow(() -> new RuntimeException("RH non trouvé"));

        conge.setStatut(Conge.StatutConge.RENVOYE_MANAGER);
        conge.setRhValidateur(rh);
        conge.setCommentaireRH(commentaire);
        conge.setDateTraitement(LocalDateTime.now());

        return congeRepository.save(conge);
    }

    @Override
    public List<Conge> getCongesByEmploye(Long employeId) {
        return congeRepository.findByEmployeId(employeId);
    }

    @Override
    public List<Conge> getCongesEnAttenteManager(Long managerId) {
        Employe manager = employeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager non trouvé"));

        if (manager.getDepartement() == null) {
            return new ArrayList<>();
        }

        return congeRepository.findByDepartementAndStatut(
                manager.getDepartement().getId(),
                Conge.StatutConge.EN_ATTENTE_MANAGER);
    }

    @Override
    public List<Conge> getCongesEnAttenteRH() {
        try {
            return congeRepository.findCongesEnAttenteRH();
        } catch (Exception e) {
            // Fallback if custom query fails
            return congeRepository.findByStatut(Conge.StatutConge.VALIDE_MANAGER);
        }
    }

    @Override
    public List<Conge> getCongesByStatut(Conge.StatutConge statut) {
        return congeRepository.findByStatut(statut);
    }

    @Override
    public Conge annulerConge(Long congeId, Long employeId) {
        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Demande de congé non trouvée"));

        if (!conge.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Vous ne pouvez annuler que vos propres demandes");
        }

        if (conge.getStatut().equals(Conge.StatutConge.VALIDE_RH)) {
            throw new RuntimeException("Impossible d'annuler un congé déjà validé par le RH");
        }

        conge.setStatut(Conge.StatutConge.ANNULE);
        return congeRepository.save(conge);
    }

    @Override
    public double calculerJoursCongesRestants(Long employeId, int annee) {
        List<Conge> congesValides = congeRepository.findByEmployeId(employeId)
                .stream()
                .filter(c -> c.getStatut().equals(Conge.StatutConge.VALIDE_RH) &&
                        c.getDateDebut().getYear() == annee)
                .toList();

        double joursPris = congesValides.stream()
                .mapToLong(c -> ChronoUnit.DAYS.between(c.getDateDebut(), c.getDateFin()) + 1)
                .sum();

        return JOURS_CONGES_ANNUEL - joursPris;
    }

    @Override
    public int getCongesUtilises(Long id) {
        try {
            return congeRepository.sumJoursUtilises(id, LocalDate.now().getYear());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getCongesRestants(Long id) {
        return JOURS_CONGES_ANNUEL - getCongesUtilises(id);
    }

    // ============ NEW METHODS ============

    @Override
    public Optional<Conge> getCongeById(Long id) {
        return congeRepository.findById(id);
    }

    @Override
    public CongeStatsDTO getStatistiques() {
        int mois = LocalDate.now().getMonthValue();
        int annee = LocalDate.now().getYear();

        long enAttente = congeRepository.countByStatut(Conge.StatutConge.VALIDE_MANAGER) +
                congeRepository.countByStatut(Conge.StatutConge.EN_ATTENTE_RH);

        long approuvesMois = 0;
        long approuvesAnnee = 0;
        long refusesMois = 0;

        try {
            approuvesMois = congeRepository.countApprouvesMois(mois, annee);
            approuvesAnnee = congeRepository.countApprouvesAnnee(annee);
            refusesMois = congeRepository.countRefusesMois(mois, annee);
        } catch (Exception e) {
            // Fallback for databases that don't support the query
        }

        double tauxApprobation = 0;
        if (approuvesMois + refusesMois > 0) {
            tauxApprobation = (double) approuvesMois / (approuvesMois + refusesMois) * 100;
        }

        Double moyenneDuree = congeRepository.avgDureeConges();
        if (moyenneDuree == null)
            moyenneDuree = 0.0;

        return CongeStatsDTO.builder()
                .enAttente(enAttente)
                .approuvesMois(approuvesMois)
                .approuvesAnnee(approuvesAnnee)
                .refusesMois(refusesMois)
                .tauxApprobation(tauxApprobation)
                .moyenneDuree(moyenneDuree)
                .congesPaye(congeRepository.countByTypeConge(Conge.TypeConge.PAYE))
                .congesMaladie(congeRepository.countByTypeConge(Conge.TypeConge.MALADIE))
                .congesMaternite(congeRepository.countByTypeConge(Conge.TypeConge.MATERNITE))
                .congesPaternite(congeRepository.countByTypeConge(Conge.TypeConge.PATERNITE))
                .congesSansSolde(congeRepository.countByTypeConge(Conge.TypeConge.SANS_SOLDE))
                .build();
    }

    @Override
    public CongeValidationDTO getValidationData(Long congeId) {
        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé"));

        Long employeId = conge.getEmploye().getId();
        int annee = LocalDate.now().getYear();

        int solde = getCongesRestants(employeId);
        int joursUtilises = getCongesUtilises(employeId);
        List<Conge> historique = getHistoriqueEmploye(employeId);

        List<Conge> absencesEquipe = new ArrayList<>();
        if (conge.getEmploye().getDepartement() != null) {
            absencesEquipe = getAbsencesEquipe(
                    conge.getEmploye().getDepartement().getId(),
                    conge.getDateDebut(),
                    conge.getDateFin());
        }

        boolean chevauchement = checkChevauchement(employeId, conge.getDateDebut(), conge.getDateFin(), congeId);

        String messageAlerte = null;
        if (chevauchement) {
            messageAlerte = "⚠️ Chevauchement détecté avec une autre demande";
        } else if (absencesEquipe.size() > 2) {
            messageAlerte = "⚠️ Plus de 2 personnes absentes sur cette période";
        }

        return CongeValidationDTO.builder()
                .conge(conge)
                .soldeConges(solde)
                .joursUtilisesAnnee(joursUtilises)
                .historiqueEmploye(historique)
                .absencesEquipe(absencesEquipe)
                .chevauchementDetecte(chevauchement)
                .messageAlerte(messageAlerte)
                .tauxAbsenteisme(0.0) // Placeholder
                .impactEquipeDetails(
                        absencesEquipe.isEmpty() ? "Faible" : "Moyen (" + absencesEquipe.size() + " absents)")
                .conformite(new HashMap<>() {
                    {
                        put("Solde suffisant",
                                solde >= (ChronoUnit.DAYS.between(conge.getDateDebut(), conge.getDateFin()) + 1));
                        put("Ancienneté > 3 mois",
                                ChronoUnit.MONTHS.between(conge.getEmploye().getDateEmbauche(), LocalDate.now()) >= 3);
                        put("Délai respecté", conge.getDateDebut().isAfter(LocalDate.now()));
                    }
                })
                .ancienneteMois(
                        (double) ChronoUnit.MONTHS.between(conge.getEmploye().getDateEmbauche(), LocalDate.now()))
                .periodeInclutFeries(false) // Placeholder
                .build();
    }

    @Override
    public List<Conge> getHistoriqueEmploye(Long employeId) {
        return congeRepository.findByEmployeIdOrderByDateDebutDesc(employeId);
    }

    @Override
    public List<Conge> getAbsencesEquipe(Long departementId, LocalDate dateDebut, LocalDate dateFin) {
        try {
            return congeRepository.findAbsencesEquipe(departementId, dateDebut, dateFin);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean checkChevauchement(Long employeId, LocalDate dateDebut, LocalDate dateFin, Long excludeId) {
        try {
            return congeRepository.existsChevauchement(employeId, dateDebut, dateFin,
                    excludeId != null ? excludeId : -1L);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Object[]> getTopEmployeesByConges(int annee) {
        try {
            List<Object[]> results = congeRepository.findTopEmployesByConges(annee);
            return results.size() > 5 ? results.subList(0, 5) : results;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Conge> getAllConges() {
        return congeRepository.findAll();
    }

    @Override
    public List<Object[]> getTendancesMensuelles(int annee) {
        // Simple implementation - can be enhanced with custom query
        List<Object[]> tendances = new ArrayList<>();
        for (int mois = 1; mois <= 12; mois++) {
            try {
                long count = congeRepository.countApprouvesMois(mois, annee);
                tendances.add(new Object[] { mois, count });
            } catch (Exception e) {
                tendances.add(new Object[] { mois, 0L });
            }
        }
        return tendances;
    }

    @Override
    public List<RhAlertDTO> getAlertes() {
        // Example: low balance alerts, pending >48h, conflicts
        List<RhAlertDTO> alerts = new ArrayList<>();
        // Low balance alert example (placeholder logic)
        // In real implementation, iterate employees and check balances
        return alerts;
    }

    @Override
    public List<Conge> getPendingOver48h() {
        // Find conges with status EN_ATTENTE_RH older than 48 hours
        LocalDateTime cutoff = LocalDateTime.now().minusHours(48);
        return congeRepository.findAll().stream()
                .filter(c -> c.getStatut() == Conge.StatutConge.EN_ATTENTE_RH && c.getCreatedAt() != null
                        && c.getCreatedAt().isBefore(cutoff))
                .toList();
    }

    @Override
    public List<CalendarEventDTO> getCalendarEvents(int month, int year) {
        List<CalendarEventDTO> events = new ArrayList<>();
        List<Conge> conges = congeRepository.findAll();
        for (Conge c : conges) {
            if (c.getDateDebut() != null && c.getDateFin() != null) {
                boolean matchesMonth = (c.getDateDebut().getMonthValue() == month && c.getDateDebut().getYear() == year)
                        ||
                        (c.getDateFin().getMonthValue() == month && c.getDateFin().getYear() == year);
                if (matchesMonth) {
                    CalendarEventDTO ev = CalendarEventDTO.builder()
                            .title(c.getEmploye().getNom() + " " + c.getEmploye().getPrenom() + " - "
                                    + c.getTypeConge())
                            .start(c.getDateDebut().toString())
                            .end(c.getDateFin().toString())
                            .type(c.getTypeConge().name())
                            .build();
                    events.add(ev);
                }
            }
        }
        return events;
    }

    @Override
    public List<RhAlertDTO> getConflicts(LocalDate dateDebut, LocalDate dateFin, int threshold) {
        // Placeholder: return empty list; real implementation would query overlapping
        // absences > threshold
        return new ArrayList<>();
    }

}