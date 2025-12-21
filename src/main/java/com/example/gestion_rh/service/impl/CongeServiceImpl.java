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
        System.out.println("📋 SERVICE: Début validerCongeManager");
        System.out.println("   - congeId: " + congeId);
        System.out.println("   - managerId: " + managerId);
        System.out.println("   - validation: " + validation);
        System.out.println("   - commentaire: " + commentaire);

        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> {
                    System.out.println("❌ Congé non trouvé: " + congeId);
                    return new RuntimeException("Demande de congé non trouvée");
                });

        System.out.println("✅ Congé trouvé: ID=" + conge.getId() + ", Statut actuel=" + conge.getStatut());

        Employe manager = employeRepository.findById(managerId)
                .orElseThrow(() -> {
                    System.out.println("❌ Manager non trouvé: " + managerId);
                    return new RuntimeException("Manager non trouvé");
                });

        System.out.println("✅ Manager trouvé: " + manager.getNom());

        // Vérification des autorisations
        if (conge.getEmploye() == null) {
            System.out.println("❌ Le congé n'a pas d'employé associé");
            throw new RuntimeException("Le congé n'a pas d'employé associé");
        }

        if (conge.getEmploye().getDepartement() == null) {
            System.out.println("❌ L'employé n'a pas de département");
            throw new RuntimeException("L'employé n'a pas de département");
        }

        if (conge.getEmploye().getDepartement().getManager() == null) {
            System.out.println("❌ Le département n'a pas de manager");
            throw new RuntimeException("Le département n'a pas de manager");
        }

        Long departementManagerId = conge.getEmploye().getDepartement().getManager().getId();
        System.out.println("🔍 Vérification autorisation:");
        System.out.println("   - Manager du département: " + departementManagerId);
        System.out.println("   - Manager connecté: " + managerId);

        if (!departementManagerId.equals(managerId)) {
            System.out.println("❌ Autorisation refusée");
            throw new RuntimeException("Vous n'êtes pas autorisé à valider cette demande");
        }

        // Vérification du statut
        System.out.println("🔍 Statut actuel du congé: " + conge.getStatut());
        if (conge.getStatut() != Conge.StatutConge.EN_ATTENTE_MANAGER) {
            System.out.println("❌ Le congé n'est pas en attente manager");
            throw new RuntimeException("Cette demande n'est plus en attente manager (statut actuel: " + conge.getStatut() + ")");
        }

        conge.setManagerValidateur(manager);

        if (validation) {
            System.out.println("✅ VALIDATION du congé");
            conge.setStatut(Conge.StatutConge.VALIDE_MANAGER);
            conge.setCommentaireManager(commentaire);
            conge.setDateValidationManager(java.time.LocalDateTime.now());
            conge.setDateRefus(null);
            conge.setMotifRefus(null);

            try {
                emailService.envoyerNotificationCongeRH(conge);
                System.out.println("📧 Email envoyé au RH");
            } catch (Exception e) {
                System.out.println("⚠️ Erreur envoi email RH: " + e.getMessage());
            }

        } else {
            System.out.println("❌ REFUS du congé");
            if (commentaire == null || commentaire.trim().isEmpty()) {
                System.out.println("❌ Motif de refus manquant");
                throw new RuntimeException("Le motif du refus est obligatoire");
            }

            conge.setStatut(Conge.StatutConge.REFUSE_MANAGER);
            conge.setMotifRefus(commentaire);
            conge.setDateRefus(java.time.LocalDateTime.now());
            conge.setDateValidationManager(null);

            try {
                emailService.envoyerNotificationRefusConge(conge, "manager");
                System.out.println("📧 Email de refus envoyé");
            } catch (Exception e) {
                System.out.println("⚠️ Erreur envoi email refus: " + e.getMessage());
            }
        }

        Conge saved = congeRepository.save(conge);
        System.out.println("💾 Congé sauvegardé: Statut=" + saved.getStatut());
        System.out.println("📋 FIN validerCongeManager");

        return saved;
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