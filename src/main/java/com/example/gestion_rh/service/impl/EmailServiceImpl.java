package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;
import com.example.gestion_rh.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url}")
    private String appUrl;

    @Override
    public void envoyerIdentifiantsEmploye(Employe employe, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(employe.getEmail());
            helper.setFrom(fromEmail);
            helper.setSubject("Vos identifiants - Plateforme RH");

            // Préparer les données pour le template
            Map<String, Object> variables = new HashMap<>();
            variables.put("nom", employe.getNom());
            variables.put("prenom", employe.getPrenom());
            variables.put("email", employe.getEmail());
            variables.put("password", password);
            variables.put("matricule", employe.getMatricule());
            variables.put("appUrl", appUrl);
            variables.put("poste", employe.getPoste());
            variables.put("role", employe.getRole());

            // Créer le contexte Thymeleaf
            Context context = new Context();
            context.setVariables(variables);

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("email/identifiants-employe", context);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email d'identifiants envoyé à : {}", employe.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email à {}", employe.getEmail(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    @Override
    public void envoyerNotificationCompteDesactive(Employe employe) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(employe.getEmail());
            helper.setFrom(fromEmail);
            helper.setSubject("Notification - Votre compte a été désactivé");

            Map<String, Object> variables = new HashMap<>();
            variables.put("nom", employe.getNom());
            variables.put("prenom", employe.getPrenom());

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process("email/compte-desactive", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Notification de désactivation envoyée à : {}", employe.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de la notification", e);
        }
    }

    @Override
    public void envoyerNotificationCongeManager(Conge conge) {

    }

    @Override
    public void envoyerNotificationCongeRH(Conge conge) {

    }

    @Override
    public void envoyerNotificationRefusConge(Conge conge, String sourceValidation) {

    }

    @Override
    public void envoyerNotificationGenerique(String destinataire, String sujet, String contenu) {

    }

    @Override
    public void envoyerNotificationCompteActive(Employe employe) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(employe.getEmail());
            helper.setFrom(fromEmail);
            helper.setSubject("Notification - Votre compte a été activé");

            Map<String, Object> variables = new HashMap<>();
            variables.put("nom", employe.getNom());
            variables.put("prenom", employe.getPrenom());
            variables.put("appUrl", appUrl);

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process("email/compte-active", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Notification d'activation envoyée à : {}", employe.getEmail());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de la notification", e);
        }
    }

    @Override
    public void envoyerEmailGenerique(String destinataire, String sujet, String contenu) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject(sujet);
            message.setText(contenu);
            message.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Email générique envoyé à : {}", destinataire);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email générique", e);
        }
    }
}