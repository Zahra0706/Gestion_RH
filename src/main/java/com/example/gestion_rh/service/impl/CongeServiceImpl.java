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
        // Vérifier les dates
        if (conge.getDateDebut().isAfter(conge.getDateFin())) {
            throw new RuntimeException("Date de début doit être avant date de fin");
        }

        // Vérifier les jours de congés restants
        long joursDemandes = ChronoUnit.DAYS.between(conge.getDateDebut(), conge.getDateFin()) + 1;
        double joursRestants = calculerJoursCongesRestants(conge.getEmploye().getId(),
                conge.getDateDebut().getYear());

        if (joursDemandes > joursRestants) {
            throw new RuntimeException("Jours de congés insuffisants");
        }

        // Définir le statut initial
        conge.setStatut(Conge.StatutConge.EN_ATTENTE_MANAGER);

        Conge savedConge = congeRepository.save(conge);

        // Envoyer notification au manager
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

        // Vérifier que le manager est bien le manager du département de l'employé
        if (!conge.getEmploye().getDepartement().getManager().getId().equals(managerId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à valider cette demande");
        }

        if (validation) {
            conge.setStatut(Conge.StatutConge.VALIDE_MANAGER);
            conge.setManagerValidateur(manager);
            conge.setCommentaireManager(commentaire);

            // Envoyer notification au RH
            emailService.envoyerNotificationCongeRH(conge);
        } else {
            conge.setStatut(Conge.StatutConge.REFUSE_MANAGER);
            conge.setManagerValidateur(manager);
            conge.setCommentaireManager(commentaire);

            // Envoyer notification à l'employé
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

        if (validation) {
            conge.setStatut(Conge.StatutConge.VALIDE_RH);
            conge.setRhValidateur(rh);
            conge.setCommentaireRH(commentaire);
        } else {
            conge.setStatut(Conge.StatutConge.REFUSE_RH);
            conge.setRhValidateur(rh);
            conge.setCommentaireRH(commentaire);

            // Envoyer notification à l'employé
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
        // Par défaut, 30 jours par an
        double joursCongesAnnuel = 30.0;

        // Compter les jours déjà pris
        List<Conge> congesValides = congeRepository.findByEmployeId(employeId)
                .stream()
                .filter(c -> c.getStatut().equals(Conge.StatutConge.VALIDE_RH) &&
                        c.getDateDebut().getYear() == annee)
                .toList();

        double joursPris = congesValides.stream()
                .mapToLong(c -> ChronoUnit.DAYS.between(c.getDateDebut(), c.getDateFin()) + 1)
                .sum();

        return joursCongesAnnuel - joursPris;
    }
}