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
    }

    @Override
    @Async
    public void envoyerNotificationCompteDesactive(Employe employe) {
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
    }

    @Override
    @Async
    public void envoyerNotificationCompteActive(Employe employe) {
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
    }

    @Override
    @Async
    public void envoyerNotificationConge(Employe employe, String sujet, String message) {
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
    }

    @Override
    public void envoyerNotificationCongeManager(Conge savedConge) {

    }

    @Override
    public void envoyerNotificationCongeRH(Conge conge) {

    }

    @Override
    public void envoyerNotificationRefusConge(Conge conge, String manager) {

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
        // Récupérer les employés du département
        // EmployeRepository employeRepository = null;
        // List<Employe> employes =
        // employeRepository.findByDepartementIdAndIsCompteActifTrue(departement.getId());
        // envoyerNotificationAnnonceImportante(annonce, employes);
    }

    @Async
    @Override
    public void envoyerRapport(String to, String subject, String body, byte[] attachment, String filename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom("zahraechokrii@gmail.com");

            if (attachment != null && filename != null) {
                org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(
                        attachment);
                helper.addAttachment(filename, resource);
            }

            mailSender.send(message);
            log.info("Rapport envoyé à {}", to);

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi du rapport à {}", to, e);
        }
    }
}