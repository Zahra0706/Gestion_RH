package com.example.gestion_rh.service.impl;

import com.example.gestion_rh.entity.Conge;
import com.example.gestion_rh.entity.Employe;
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
            context.setVariable("applicationUrl", "http://localhost:8080");

            String htmlContent = templateEngine.process("email/identifiants-employe", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(employe.getEmail());
            helper.setSubject("HR Manager - Vos identifiants de connexion");
            helper.setText(htmlContent, true);

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
}