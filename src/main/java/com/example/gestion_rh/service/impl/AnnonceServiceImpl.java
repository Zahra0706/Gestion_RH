package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Annonce;
import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.AnnonceRepository;
import com.example.gestion_rh.repository.DepartementRepository;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.AnnonceService;
import com.example.gestion_rh.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.gestion_rh.repository.spec.AnnonceSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AnnonceServiceImpl implements AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final EmailService emailService;

    @Override
    public Annonce createAnnonce(Annonce annonce, Long createurId) {
        try {
            Employe createur = employeRepository.findById(createurId)
                    .orElseThrow(() -> new RuntimeException("Créateur non trouvé"));

            annonce.setCreateur(createur);
            annonce.setDatePublication(LocalDateTime.now());

            // Si c'est une annonce de département, vérifier le département
            if (annonce.getDepartement() != null && annonce.getDepartement().getId() != null) {
                Departement departement = departementRepository.findById(annonce.getDepartement().getId())
                        .orElseThrow(() -> new RuntimeException("Département non trouvé"));
                annonce.setDepartement(departement);
            } else {
                annonce.setDepartement(null);
            }

            Annonce savedAnnonce = annonceRepository.save(annonce);

            // Notifier les utilisateurs concernés
            notifyUsersAboutAnnonce(savedAnnonce);

            return savedAnnonce;
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'annonce", e);
            throw new RuntimeException("Erreur lors de la création de l'annonce: " + e.getMessage());
        }
    }

    @Override
    public Annonce updateAnnonce(Long id, Annonce annonce) {
        try {
            Annonce existingAnnonce = annonceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Annonce non trouvée avec l'ID: " + id));

            existingAnnonce.setTitre(annonce.getTitre());
            existingAnnonce.setContenu(annonce.getContenu());
            existingAnnonce.setType(annonce.getType());

            if (annonce.getDepartement() != null && annonce.getDepartement().getId() != null) {
                Departement departement = departementRepository.findById(annonce.getDepartement().getId())
                        .orElse(null);
                existingAnnonce.setDepartement(departement);
            } else {
                existingAnnonce.setDepartement(null);
            }

            return annonceRepository.save(existingAnnonce);
        } catch (Exception e) {
            log.error("Erreur lors de la modification de l'annonce ID: {}", id, e);
            throw new RuntimeException("Erreur lors de la modification: " + e.getMessage());
        }
    }

    @Override
    public void deleteAnnonce(Long id) {
        try {
            if (!annonceRepository.existsById(id)) {
                throw new RuntimeException("Annonce non trouvée avec l'ID: " + id);
            }
            annonceRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'annonce ID: {}", id, e);
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @Override
    public Annonce getAnnonceById(Long id) {
        return annonceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée avec l'ID: " + id));
    }

    @Override
    public List<Annonce> getAllAnnonces() {
        try {
            return annonceRepository.findAllByOrderByDatePublicationDesc();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des annonces", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Annonce> getAnnoncesByDepartement(Long departementId) {
        try {
            return annonceRepository.findByDepartementId(departementId);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des annonces par département ID: {}", departementId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Annonce> getAnnoncesByCreateur(Long createurId) {
        try {
            return annonceRepository.findByCreateurId(createurId);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des annonces par créateur ID: {}", createurId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Annonce> getAnnoncesByType(Annonce.TypeAnnonce type) {
        try {
            return annonceRepository.findByType(type);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des annonces par type: {}", type, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Annonce> getAnnoncesForEmploye(Long employeId) {
        try {
            Employe employe = employeRepository.findById(employeId)
                    .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'ID: " + employeId));

            // Si l'employé a un département, on récupère les annonces de son département +
            // générales
            if (employe.getDepartement() != null) {
                return annonceRepository.findAnnoncesForDepartement(employe.getDepartement().getId());
            } else {
                // Si pas de département, seulement les annonces générales
                return annonceRepository.findByDepartementIsNullOrderByDatePublicationDesc();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des annonces pour l'employé ID: {}", employeId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Annonce> getImportantAnnonces() {
        try {
            return annonceRepository.findByTypeOrderByDatePublicationDesc(Annonce.TypeAnnonce.IMPORTANTE);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des annonces importantes", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Page<Annonce> getAnnonces(String keyword, Annonce.TypeAnnonce type, Long departementId, Pageable pageable) {
        Specification<Annonce> spec = AnnonceSpecification.getSpec(keyword, type, departementId);
        return annonceRepository.findAll(spec, pageable);
    }

    @Override
    public void notifyUsersAboutAnnonce(Annonce annonce) {
        try {
            List<Employe> recipients = getRecipientsForAnnonce(annonce);

            if (recipients.isEmpty()) {
                log.warn("Aucun destinataire trouvé pour l'annonce ID: {}", annonce.getId());
                return;
            }

            log.info("Envoi des notifications pour l'annonce '{}' à {} destinataires",
                    annonce.getTitre(), recipients.size());

            // Envoyer les notifications (dans un vrai projet, utiliser un système de queue)
            for (Employe recipient : recipients) {
                // Ne pas notifier le créateur de l'annonce
                if (annonce.getCreateur() != null && recipient.getId().equals(annonce.getCreateur().getId())) {
                    continue;
                }

                try {
                    sendEmailNotification(annonce, recipient);
                } catch (Exception e) {
                    log.error("Erreur lors de l'envoi de la notification à {}",
                            recipient.getEmail(), e);
                }
            }

        } catch (Exception e) {
            log.error("Erreur lors de la notification pour l'annonce ID: {}", annonce.getId(), e);
        }
    }

    private List<Employe> getRecipientsForAnnonce(Annonce annonce) {
        try {
            // Règle 1 & 2 : Si un département est spécifié, on envoie uniquement aux
            // employés de ce département
            // (Quelle que soit le type de l'annonce)
            if (annonce.getDepartement() != null) {
                return employeRepository.findByDepartementIdAndIsCompteActifTrue(
                        annonce.getDepartement().getId());
            }

            // Si aucun département n'est spécifié :
            switch (annonce.getType()) {
                case DEPARTEMENT:
                    // Règle 1: Type DÉPARTEMENT sans département spécifié -> Personne
                    log.warn(
                            "Annonce de type DÉPARTEMENT créée sans département spécifié. Aucune notification envoyée.");
                    return new ArrayList<>();

                case GENERALE:
                case IMPORTANTE:
                case INFORMATION:
                    // Règle 2: Autres types sans département -> Tous les employés actifs
                    return employeRepository.findByIsCompteActifTrue();

                default:
                    return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des destinataires", e);
            return new ArrayList<>();
        }
    }

    private void sendEmailNotification(Annonce annonce, Employe recipient) {
        try {
            String sujet = "HR Manager - Nouvelle annonce : " + annonce.getTitre();

            String message = "<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                    "<h2>Bonjour " + recipient.getPrenom() + " " + recipient.getNom() + ",</h2>" +
                    "<p>Une nouvelle annonce a été publiée sur la plateforme HR Manager.</p>" +
                    "<div style='background-color: #f8f9fa; padding: 20px; border-left: 4px solid #007bff; margin: 20px 0; border-radius: 0 5px 5px 0;'>"
                    +
                    "<h3 style='color: #007bff; margin-top: 0;'>" + annonce.getTitre() + "</h3>" +
                    "<p><strong>Type :</strong> " + annonce.getType() + "</p>" +
                    "<p><strong>Date :</strong> " + annonce.getDatePublication() + "</p>" +
                    "<p><strong>Publié par :</strong> " + annonce.getCreateur().getPrenom() + " "
                    + annonce.getCreateur().getNom() + "</p>" +
                    "<div style='margin-top: 15px; padding: 10px; background: white; border-radius: 5px;'>" +
                    "<strong>Contenu :</strong>" +
                    "<p>" + annonce.getContenu() + "</p>" +
                    "</div></div>" +
                    "<p>Vous pouvez consulter cette annonce sur la plateforme HR Manager.</p>" +
                    "<a href='http://localhost:8085/annonces/" + annonce.getId()
                    + "' style='background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;'>Voir l'annonce complète</a>"
                    +
                    "<br><br>" +
                    "<p>Cordialement,<br><strong>L'équipe HR Manager</strong></p>" +
                    "</body></html>";

            emailService.envoyerNotificationConge(recipient, sujet, message);

        } catch (Exception e) {
            log.error("Erreur lors de la préparation de l'email pour {}", recipient.getEmail(), e);
        }
    }
}
