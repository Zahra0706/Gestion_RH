package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.*;
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

    // ✅ AJOUT IMPORTANT : pour éviter NullPointer dans envoyerNotificationAnnonceDepartement
    private final EmployeRepository employeRepository;

    // =====================================================
    // ✅ Identifiants Employé (template + fallback texte)
    // =====================================================
    @Override
    @Async
    public void envoyerIdentifiantsEmploye(Employe employe, String password) {

        if (employe.getEmail() == null) {
            log.warn("Pas d'email pour l'employé {}", employe.getId());
            return;
        }

        // ✅ 1) Envoi Template HTML
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

            mailSender.send(message);
            log.info("Email d'identifiants envoyé (HTML) à {}", employe.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi HTML à {}", employe.getEmail(), e);
        }

        // ✅ 2) Envoi texte simple fallback
        String sujet = "Vos identifiants de connexion - TECHSOLUTIONS";
        String contenu = String.format(
                "Bonjour %s %s,\n\nBienvenue chez TECHSOLUTIONS!\n\n" +
                        "Voici vos identifiants de connexion:\n" +
                        "Email: %s\nMot de passe temporaire: %s\n\n" +
                        "Veuillez changer votre mot de passe lors de votre première connexion.\n\n" +
                        "Cordialement,\nService RH",
                employe.getPrenom(), employe.getNom(),
                employe.getEmail(), password
        );

        envoyerEmail(employe.getEmail(), sujet, contenu);
    }

    // =====================================================
    // ✅ Compte désactivé
    // =====================================================
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
            log.info("Notification désactivation envoyée (HTML) à {}", employe.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi désactivation HTML à {}", employe.getEmail(), e);
        }

        String sujet = "Votre compte a été désactivé";
        String contenu = String.format(
                "Bonjour %s,\n\nVotre compte a été désactivé. Veuillez contacter le service RH.\n\nCordialement,\nService RH",
                employe.getPrenom()
        );

        envoyerEmail(employe.getEmail(), sujet, contenu);
    }

    // =====================================================
    // ✅ Compte activé
    // =====================================================
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
            log.info("Notification activation envoyée (HTML) à {}", employe.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi activation HTML à {}", employe.getEmail(), e);
        }

        String sujet = "Votre compte a été réactivé";
        String contenu = String.format(
                "Bonjour %s,\n\nVotre compte a été réactivé. Vous pouvez maintenant vous connecter.\n\nCordialement,\nService RH",
                employe.getPrenom()
        );

        envoyerEmail(employe.getEmail(), sujet, contenu);
    }

    // =====================================================
    // ✅ Notification congé (générique)
    // =====================================================
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
            log.info("Notification congé envoyée (HTML) à {}", employe.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur notification congé HTML à {}", employe.getEmail(), e);
        }

        envoyerEmail(employe.getEmail(), sujet, message);
    }

    // =====================================================
    // ✅ Congé vers Manager
    // =====================================================
    @Override
    public void envoyerNotificationCongeManager(Conge conge) {

        if (conge.getEmploye() == null ||
                conge.getEmploye().getDepartement() == null ||
                conge.getEmploye().getDepartement().getManager() == null ||
                conge.getEmploye().getDepartement().getManager().getEmail() == null) {
            log.warn("Pas de manager avec email pour le congé {}", conge.getId());
            return;
        }

        String destinataire = conge.getEmploye().getDepartement().getManager().getEmail();
        String sujet = "Nouvelle demande de congé - " + conge.getEmploye().getNom();

        String contenu = String.format(
                "Bonjour,\n\nUne nouvelle demande de congé nécessite votre validation:\n\n" +
                        "Employé: %s %s\nType: %s\nPériode: du %s au %s\nMotif: %s\n\n" +
                        "Veuillez vous connecter pour traiter cette demande.\n\nCordialement,\nService RH",
                conge.getEmploye().getPrenom(), conge.getEmploye().getNom(),
                (conge.getTypeConge() != null ? conge.getTypeConge().name() : "Non spécifié"),
                conge.getDateDebut(), conge.getDateFin(),
                (conge.getMotif() != null ? conge.getMotif() : "Non spécifié")
        );

        envoyerEmail(destinataire, sujet, contenu);
    }

    // =====================================================
    // ✅ Congé vers RH
    // =====================================================
    @Override
    public void envoyerNotificationCongeRH(Conge conge) {
        String destinataire = "rh@techsolutions.ma";
        String sujet = "Demande de congé validée par le manager - " + conge.getEmploye().getNom();

        String contenu = String.format(
                "Bonjour,\n\nUne demande de congé a été validée par le manager et nécessite votre validation:\n\n" +
                        "Employé: %s %s\nType: %s\nPériode: du %s au %s\nCommentaire manager: %s\n\n" +
                        "Veuillez vous connecter pour traiter cette demande.\n\nCordialement",
                conge.getEmploye().getPrenom(), conge.getEmploye().getNom(),
                (conge.getTypeConge() != null ? conge.getTypeConge().name() : "Non spécifié"),
                conge.getDateDebut(), conge.getDateFin(),
                (conge.getCommentaireManager() != null ? conge.getCommentaireManager() : "Aucun")
        );

        envoyerEmail(destinataire, sujet, contenu);
    }

    // =====================================================
    // ✅ Refus congé
    // =====================================================
    @Override
    public void envoyerNotificationRefusConge(Conge conge, String source) {

        if (conge.getEmploye() == null || conge.getEmploye().getEmail() == null) {
            log.warn("Pas d'email pour employé dans congé {}", conge.getId());
            return;
        }

        String destinataire = conge.getEmploye().getEmail();
        String sujet = "Demande de congé refusée";

        String commentaire = "manager".equals(source)
                ? conge.getCommentaireManager()
                : conge.getCommentaireRH();

        String contenu = String.format(
                "Bonjour %s,\n\nVotre demande de congé du %s au %s a été refusée par %s.\n\n" +
                        "Commentaire: %s\n\nCordialement,\nService RH",
                conge.getEmploye().getPrenom(),
                conge.getDateDebut(), conge.getDateFin(),
                source,
                commentaire != null ? commentaire : "Non spécifié"
        );

        envoyerEmail(destinataire, sujet, contenu);
    }

    // =====================================================
    // ✅ Notifications d'annonces
    // =====================================================
    @Async
    @Override
    public void envoyerNotificationAnnonce(Annonce annonce, Employe recipient) {
        if (recipient.getEmail() == null) return;

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

        } catch (MessagingException e) {
            log.error("Erreur envoi annonce à {}", recipient.getEmail(), e);
        }
    }

    @Async
    @Override
    public void envoyerNotificationAnnonceImportante(Annonce annonce, List<Employe> recipients) {
        recipients.forEach(r -> envoyerNotificationAnnonce(annonce, r));
    }

    @Async
    @Override
    public void envoyerNotificationAnnonceDepartement(Annonce annonce, Departement departement) {
        List<Employe> employes = employeRepository
                .findByDepartementIdAndIsCompteActifTrue(departement.getId());
        envoyerNotificationAnnonceImportante(annonce, employes);
    }

    // =====================================================
    // ✅ PDF contract / rapport
    // =====================================================
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

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    @Override
    public void envoyerRapport(String destinataire, String sujet, String contenu, byte[] attachment, String nomFichier) {
        envoyerContratParEmail(destinataire, sujet, contenu, attachment, nomFichier);
    }

    // =====================================================
    // ✅ Email simple
    // =====================================================
    @Override
    public void envoyerEmail(String destinataire, String sujet, String contenu) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject(sujet);
            message.setText(contenu);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    // =====================================================
    // ✅ Paie
    // =====================================================
    @Override
    public void envoyerFicheDePaie(Salaire salaire, Employe employe) {
        if (employe.getEmail() == null) return;

        String sujet = "Fiche de paie - " + salaire.getMoisAnnee();
        String contenu = String.format(
                "Bonjour %s %s,\n\nVeuillez trouver votre fiche de paie pour %s.\n\nCordialement,\nService RH",
                employe.getPrenom(), employe.getNom(), salaire.getMoisAnnee()
        );

        envoyerEmail(employe.getEmail(), sujet, contenu);
    }

    @Override
    public void envoyerRappelSalaireNonValide(Employe rh, List<Salaire> salairesEnAttente) {
        if (rh.getEmail() == null) return;

        String sujet = "Rappel : Salaires en attente de validation";
        String contenu = "Bonjour,\n\nIl y a " + salairesEnAttente.size() +
                " salaires en attente de validation.\n\nCordialement,\nSystème RH";

        envoyerEmail(rh.getEmail(), sujet, contenu);
    }

    @Override
    public void envoyerConfirmationPaiement(Salaire salaire) {
        if (salaire.getEmploye() == null || salaire.getEmploye().getEmail() == null) return;

        String sujet = "Avis de virement - Salaire " + salaire.getMoisAnnee();
        String contenu = String.format(
                "Bonjour,\n\nVotre salaire du mois de %s a été viré.\nMontant Net: %.2f Dhs\n\nCordialement,\nService RH",
                salaire.getMoisAnnee(),
                salaire.getSalaireNet()
        );

        envoyerEmail(salaire.getEmploye().getEmail(), sujet, contenu);
    }
}
