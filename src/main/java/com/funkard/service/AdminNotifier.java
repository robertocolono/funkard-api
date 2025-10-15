package com.funkard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminNotifier {

    private final EmailService emailService;

    @Value("${admin.email:}")
    private String adminEmail;

    public AdminNotifier(EmailService emailService) {
        this.emailService = emailService;
    }

    public void notifyMissingHistory(String itemName, String category, String rangeType) {
        // Niente persistenza. Solo email (se configurata) + log.
        String subject = "Funkard | Storico insufficiente: " + itemName + " [" + rangeType + "]";
        String body = "Item: " + itemName + "\nCategoria: " + category + "\nRange: " + rangeType +
                      "\nAzione: valida/stima iniziale dal pannello admin.\n";
        System.out.println("ADMIN NOTICE -> " + subject + "\n" + body);
        if (adminEmail != null && !adminEmail.isBlank()) {
            try { 
                emailService.sendSimple(adminEmail, subject, body); 
            } catch (Exception ignored) {
                // Log dell'errore ma non bloccare l'applicazione
                System.err.println("Errore invio email admin: " + ignored.getMessage());
            }
        }
    }
}
