package com.funkard.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        String subject = "Verifica il tuo account Funkard";
        String verifyUrl = "https://funkard.com/api/auth/verify?token=" + token;
        String content = """
            <h2>Benvenuto su Funkard!</h2>
            <p>Clicca sul pulsante qui sotto per verificare il tuo account:</p>
            <a href="%s" style="background-color:#f2b237;color:black;padding:10px 20px;border-radius:8px;text-decoration:none;">
                Verifica il mio account
            </a>
            <p>Il link scadrà tra 24 ore.</p>
        """.formatted(verifyUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Errore durante l'invio dell'email", e);
        }
    }
}