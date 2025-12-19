package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.CongeRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.CongeService;
import com.example.gestion_rh.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CongeServiceImpl implements CongeService {

    private final CongeRepository congeRepository;
    private final EmployeRepository employeRepository;
    private final EmailService emailService;

    @Override
    public Conge demanderConge(Conge conge) {
        if (conge.getDateDebut().isAfter(conge.getDateFin())) {
            throw new RuntimeException("Date de début doit être avant date de fin");
        }

        long joursDemandes = ChronoUnit.DAYS.between(conge.getDateDebut(), conge.getDateFin()) + 1;

        double joursRestants = calculerJoursCongesRestants(
                conge.getEmploye().getId(),
                conge.getDateDebut().getYear()
        );

        if (joursDemandes > joursRestants) {
            throw new RuntimeException("Jours de congés insuffisants");
        }

        conge.setStatut(Conge.StatutConge.EN_ATTENTE_MANAGER);
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

        // ✅ Autorisation: le manager connecté doit être le manager du département de l'employé demandeur
        if (conge.getEmploye() == null || conge.getEmploye().getDepartement() == null
                || conge.getEmploye().getDepartement().getManager() == null
                || !conge.getEmploye().getDepartement().getManager().getId().equals(managerId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à valider cette demande");
        }

        // ✅ Seulement si en attente manager
        if (conge.getStatut() != Conge.StatutConge.EN_ATTENTE_MANAGER) {
            throw new RuntimeException("Cette demande n'est plus en attente manager");
        }

        conge.setManagerValidateur(manager);

        if (validation) {
            conge.setStatut(Conge.StatutConge.VALIDE_MANAGER);
            conge.setCommentaireManager(commentaire);
            conge.setDateValidationManager(java.time.LocalDateTime.now());
            conge.setDateRefus(null);
            conge.setMotifRefus(null);

            emailService.envoyerNotificationCongeRH(conge);

        } else {
            // commentaire ici = motifRefus (car ton controller envoie motifRefus dans ce param)
            if (commentaire == null || commentaire.trim().isEmpty()) {
                throw new RuntimeException("Le motif du refus est obligatoire");
            }

            conge.setStatut(Conge.StatutConge.REFUSE_MANAGER);
            conge.setMotifRefus(commentaire);
            conge.setDateRefus(java.time.LocalDateTime.now());
            conge.setDateValidationManager(null);

            emailService.envoyerNotificationRefusConge(conge, "manager");
        }

        return congeRepository.save(conge);
    }

    @Override
    public Conge validerCongeRH(Long congeId, Long rhId, boolean validation, String commentaire) {
        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Demande de congé non trouvée"));

        if (!conge.getStatut().equals(Conge.StatutConge.VALIDE_MANAGER)) {
            throw new RuntimeException("La demande doit d'abord être validée par le manager");
        }

        Employe rh = employeRepository.findById(rhId)
                .orElseThrow(() -> new RuntimeException("RH non trouvé"));

        conge.setRhValidateur(rh);
        conge.setCommentaireRH(commentaire);

        if (validation) {
            conge.setStatut(Conge.StatutConge.VALIDE_RH);
        } else {
            conge.setStatut(Conge.StatutConge.REFUSE_RH);
            emailService.envoyerNotificationRefusConge(conge, "RH");
        }

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
            throw new RuntimeException("Le manager n'est associé à aucun département");
        }

        return congeRepository.findByDepartementAndStatut(
                manager.getDepartement().getId(),
                Conge.StatutConge.EN_ATTENTE_MANAGER
        );
    }

    @Override
    public List<Conge> getCongesEnAttenteRH() {
        return congeRepository.findByStatut(Conge.StatutConge.VALIDE_MANAGER);
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
        double joursCongesAnnuel = 30.0;

        List<Conge> congesValides = congeRepository.findByEmployeId(employeId)
                .stream()
                .filter(c -> c.getStatut().equals(Conge.StatutConge.VALIDE_RH)
                        && c.getDateDebut().getYear() == annee)
                .toList();

        double joursPris = congesValides.stream()
                .mapToLong(c -> ChronoUnit.DAYS.between(c.getDateDebut(), c.getDateFin()) + 1)
                .sum();

        return joursCongesAnnuel - joursPris;
    }

    // ✅ AJOUTS POUR LE MANAGER

    @Override
    public List<Conge> getCongesDepartementByStatut(Long departementId, Conge.StatutConge statut) {
        return congeRepository.findByDepartementAndStatut(departementId, statut);
    }

    @Override
    public List<Conge> getHistoriqueCongesDepartement(Long departementId) {
        List<Conge.StatutConge> statutsHistorique = Arrays.asList(
                Conge.StatutConge.VALIDE_MANAGER,
                Conge.StatutConge.REFUSE_MANAGER,
                Conge.StatutConge.VALIDE_RH,
                Conge.StatutConge.REFUSE_RH,
                Conge.StatutConge.ANNULE
        );

        return congeRepository.findHistoriqueByDepartement(departementId, statutsHistorique);
    }
}