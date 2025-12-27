package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Annonce;
import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Departement;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.repository.EmployeRepository;
import com.example.gestion_rh.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void envoyerIdentifiantsEmploye(Employe employe, String password) {
        if (employe.getEmail() == null) {
            log.warn("Pas d'email pour l'employé {}", employe.getId());
            return;
        }


        try {
            Context context = new Context();
            context.setVariable("employe", employe);
            context.setVariable("password", password);
            context.setVariable("applicationUrl", "http://localhost:8085");

            String htmlContent = templateEngine.process("email/identifiants-employe", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(employe.getEmail());
            helper.setSubject("HR Manager - Vos identifiants de connexion");
            helper.setText(htmlContent, true);
            helper.setFrom("zahraechokrii@gmail.com");

            log.info("Envoi de l'email...");
            mailSender.send(message);
            log.info("Email d'identifiants envoyé à {}", employe.getEmail());
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email à {}", employe.getEmail(), e);
        }


        String sujet = "Vos identifiants de connexion - TECHSOLUTIONS";
        String contenu = String.format(
                "Bonjour %s %s,\n\n" +
                        "Bienvenue chez TECHSOLUTIONS!\n\n" +
                        "Voici vos identifiants de connexion:\n" +
                        "Email: %s\n" +
                        "Mot de passe temporaire: %s\n\n" +
                        "Veuillez changer votre mot de passe lors de votre première connexion.\n\n" +
                        "Cordialement,\nService RH",
                employe.getPrenom(), employe.getNom(),
                employe.getEmail(),
                password);

        envoyerEmail(employe.getEmail(), sujet, contenu);
    }

    @Override
    @Async
    public void envoyerNotificationCompteDesactive(Employe employe) {
        if (employe.getEmail() == null) {
            log.warn("Pas d'email pour l'employé {}", employe.getId());
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("employe", employe);
            context.setVariable("applicationUrl", "http://localhost:8080");

            String htmlContent = templateEngine.process("email/compte-desactive", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(employe.getEmail());
            helper.setSubject("HR Manager - Compte désactivé");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Notification de désactivation envoyée à {}", employe.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de la notification à {}", employe.getEmail(), e);
        }


        String sujet = "Votre compte a été désactivé";
        String contenu = String.format(
                "Bonjour %s,\n\nVotre compte a été désactivé. Veuillez contacter le service RH pour plus d'informations.\n\nCordialement,\nService RH",
                employe.getPrenom());

        envoyerEmail(employe.getEmail(), sujet, contenu);
    }

    @Override
    @Async
    public void envoyerNotificationCompteActive(Employe employe) {
        if (employe.getEmail() == null) {
            log.warn("Pas d'email pour l'employé {}", employe.getId());
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("employe", employe);
            context.setVariable("applicationUrl", "http://localhost:8080");

            String htmlContent = templateEngine.process("email/compte-active", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(employe.getEmail());
            helper.setSubject("HR Manager - Compte activé");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Notification d'activation envoyée à {}", employe.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de la notification à {}", employe.getEmail(), e);
        }


        String sujet = "Votre compte a été réactivé";
        String contenu = String.format(
                "Bonjour %s,\n\nVotre compte a été réactivé. Vous pouvez maintenant vous connecter.\n\nCordialement,\nService RH",
                employe.getPrenom());

        envoyerEmail(employe.getEmail(), sujet, contenu);
    }

    @Override
    @Async
    public void envoyerNotificationConge(Employe employe, String sujet, String message) {
        if (employe.getEmail() == null) {
            log.warn("Pas d'email pour l'employé {}", employe.getId());
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(employe.getEmail());
            helper.setSubject("HR Manager - " + sujet);
            helper.setText(message, true);

            mailSender.send(mimeMessage);
            log.info("Notification de congé envoyée à {}", employe.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de la notification de congé à {}", employe.getEmail(), e);
        }

        envoyerEmail(employe.getEmail(), sujet, message);
    }

    @Override
    public void envoyerNotificationCongeManager(Conge conge) {

        if (conge.getEmploye().getDepartement() == null ||
                conge.getEmploye().getDepartement().getManager() == null ||
                conge.getEmploye().getDepartement().getManager().getEmail() == null) {
            log.warn("Pas de manager avec email pour le congé {}", conge.getId());
            return;
        }

        String destinataire = conge.getEmploye().getDepartement().getManager().getEmail();
        String sujet = "Nouvelle demande de congé - " + conge.getEmploye().getNom();
        String contenu = String.format(
                "Bonjour,\n\nUne nouvelle demande de congé nécessite votre validation:\n\n" +
                        "Employé: %s %s\n" +
                        "Type: %s\n" +
                        "Période: du %s au %s\n" +
                        "Motif: %s\n\n" +
                        "Veuillez vous connecter pour traiter cette demande.\n\nCordialement,\nService RH",
                conge.getEmploye().getPrenom(), conge.getEmploye().getNom(),
                conge.getTypeConge(),
                conge.getDateDebut(), conge.getDateFin(),
                conge.getMotif() != null ? conge.getMotif() : "Non spécifié");

        envoyerEmail(destinataire, sujet, contenu);
    }

    @Override
    public void envoyerNotificationCongeRH(Conge conge) {
        String destinataire = "rh@techsolutions.ma";
        String sujet = "Demande de congé validée par le manager - " + conge.getEmploye().getNom();
        String contenu = String.format(
                "Bonjour,\n\nUne demande de congé a été validée par le manager et nécessite votre validation:\n\n" +
                        "Employé: %s %s\n" +
                        "Type: %s\n" +
                        "Période: du %s au %s\n" +
                        "Commentaire manager: %s\n\n" +
                        "Veuillez vous connecter pour traiter cette demande.\n\nCordialement",
                conge.getEmploye().getPrenom(), conge.getEmploye().getNom(),
                conge.getTypeConge(),
                conge.getDateDebut(), conge.getDateFin(),
                conge.getCommentaireManager() != null ? conge.getCommentaireManager() : "Aucun");

        envoyerEmail(destinataire, sujet, contenu);
    }

    @Override
    public void envoyerNotificationRefusConge(Conge conge, String source) {
        if (conge.getEmploye().getEmail() == null) {
            log.warn("Pas d'email pour l'employé {}", conge.getEmploye().getId());
            return;
        }

        String destinataire = conge.getEmploye().getEmail();
        String sujet = "Demande de congé refusée";
        String commentaire = source.equals("manager") ? conge.getCommentaireManager() : conge.getCommentaireRH();
        String contenu = String.format(
                "Bonjour %s,\n\nVotre demande de congé du %s au %s a été refusée par %s.\n\n" +
                        "Commentaire: %s\n\n" +
                        "Cordialement,\nService RH",
                conge.getEmploye().getPrenom(),
                conge.getDateDebut(), conge.getDateFin(),
                source,
                commentaire != null ? commentaire : "Non spécifié");

        envoyerEmail(destinataire, sujet, contenu);
    }

    @Async
    @Override
    public void envoyerNotificationAnnonce(Annonce annonce, Employe recipient) {
        try {
            Context context = new Context();
            context.setVariable("annonce", annonce);
            context.setVariable("recipient", recipient);
            context.setVariable("applicationUrl", "http://localhost:8085");

            String htmlContent = templateEngine.process("email/annonce-notification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipient.getEmail());
            helper.setSubject("HR Manager - Nouvelle annonce : " + annonce.getTitre());
            helper.setText(htmlContent, true);
            helper.setFrom("zahraechokrii@gmail.com");

            mailSender.send(message);
            log.info("Notification d'annonce envoyée à {}", recipient.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de la notification d'annonce à {}",
                    recipient.getEmail(), e);
        }
    }

    @Async
    @Override
    public void envoyerNotificationAnnonceImportante(Annonce annonce, List<Employe> recipients) {
        recipients.forEach(recipient -> envoyerNotificationAnnonce(annonce, recipient));
    }

    @Async
    @Override
    public void envoyerNotificationAnnonceDepartement(Annonce annonce, Departement departement) {
        EmployeRepository employeRepository = null;
        List<Employe> employes = employeRepository.findByDepartementIdAndIsCompteActifTrue(departement.getId());
        envoyerNotificationAnnonceImportante(annonce, employes);
    }

    @Override
    public void envoyerContratParEmail(String destinataire, String sujet, String contenu,
                                       byte[] pdfAttachment, String nomFichier) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(contenu, true);

            if (pdfAttachment != null && pdfAttachment.length > 0) {
                helper.addAttachment(nomFichier, new ByteArrayResource(pdfAttachment));
            }

            mailSender.send(message);
            log.info("Email envoyé avec succès à: {}", destinataire);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", destinataire, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    @Override
    public void envoyerEmail(String destinataire, String sujet, String contenu) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject(sujet);
            message.setText(contenu);

            mailSender.send(message);
            log.info("Email simple envoyé avec succès à: {}", destinataire);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", destinataire, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    @Override
    public void envoyerRapport(String destinataire, String sujet, String contenu, byte[] attachment,
                               String nomFichier) {
        envoyerContratParEmail(destinataire, sujet, contenu, attachment, nomFichier);
    }

    @Override
    public void envoyerFicheDePaie(com.example.gestion_rh.entity.Salaire salaire, Employe employe) {
        if (employe.getEmail() == null) {
            log.warn("Pas d'email pour l'employé {}", employe.getId());
            return;
        }

        String sujet = "Fiche de paie - " + salaire.getMoisAnnee();
        String contenu = String.format(
                "Bonjour %s %s,\n\nVeuillez trouver ci-joint votre fiche de paie pour le mois de %s.\n\nCordialement,\nService RH",
                employe.getPrenom(), employe.getNom(), salaire.getMoisAnnee());

        envoyerEmail(employe.getEmail(), sujet, contenu);
    }

    @Override
    public void envoyerRappelSalaireNonValide(Employe rh,
                                              java.util.List<com.example.gestion_rh.entity.Salaire> salairesEnAttente) {
        if (rh.getEmail() == null)
            return;

        String sujet = "Rappel : Salaires en attente de validation";
        String contenu = String.format(
                "Bonjour,\n\nIl y a %d salaires en attente de validation.\nVeuillez les traiter dans les plus brefs délais.\n\nCordialement,\nSystème RH",
                salairesEnAttente.size());

        envoyerEmail(rh.getEmail(), sujet, contenu);
    }

    @Override
    public void envoyerConfirmationPaiement(com.example.gestion_rh.entity.Salaire salaire) {
        if (salaire.getEmploye().getEmail() == null)
            return;

        String sujet = "Avis de virement - Salaire " + salaire.getMoisAnnee();
        String contenu = String.format(
                "Bonjour,\n\nVotre salaire du mois de %s a été viré sur votre compte.\nMontant Net: %.2f Dhs\n\nCordialement,\nService RH",
                salaire.getMoisAnnee(),
                salaire.getSalaireNet());

        envoyerEmail(salaire.getEmploye().getEmail(), sujet, contenu);
    }
}