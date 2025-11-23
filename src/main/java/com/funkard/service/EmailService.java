package com.funkard.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 📧 Service per invio email con Fallback Automatico
 * 
 * Funzionalità:
 * - Email verifica account
 * - Email conferma cancellazione account (GDPR)
 * - Supporto localizzazione IT/EN
 * - Fallback automatico su support@funkard.com in caso di errore SMTP
 * - Logging dettagliato per audit
 */
@Service
@Slf4j
public class EmailService {

    @Autowired
    @Qualifier("primaryMailSender")
    private JavaMailSender primaryMailSender;
    
    @Autowired
    @Qualifier("fallbackMailSender")
    private JavaMailSender fallbackMailSender;
    
    @Value("${MAIL_FROM:no-reply@funkard.com}")
    private String mailFrom;
    
    @Value("${MAIL_FROM_NAME:Funkard}")
    private String mailFromName;
    
    @Value("${MAIL_FALLBACK:support@funkard.com}")
    private String mailFallback;
    
    @Value("${admin.email:legal@funkard.com}")
    private String adminEmail;
    
    @Autowired
    private EmailTemplateManager templateManager;
    
    @Autowired
    private EmailLogService logService;

    /**
     * 📧 Metodo generico per invio email con fallback automatico e logging
     * 
     * @param to Email destinatario
     * @param subject Oggetto email
     * @param bodyHtml Corpo email (HTML o testo)
     * @param isHtml true se HTML, false se testo
     * @param emailType Tipo email (es. ACCOUNT_CONFIRMATION)
     * @param locale Locale (it, en)
     * @param templateName Nome template usato
     * @return UUID del log creato, null se fallito
     */
    public java.util.UUID sendEmail(String to, String subject, String bodyHtml, boolean isHtml, 
                                   String emailType, String locale, String templateName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String actualSender = mailFrom;
        String errorMessage = null;
        boolean success = false;
        
        // Tentativo con mittente principale (no-reply@funkard.com)
        try {
            sendUsing(primaryMailSender, mailFrom, mailFromName, to, subject, bodyHtml, isHtml);
            log.info("✅ [{}] Email inviata da {} a {} - Oggetto: {}", timestamp, mailFrom, to, subject);
            success = true;
        } catch (Exception ex) {
            log.warn("⚠️ [{}] Errore invio da {} a {}: {} - Tentativo con fallback...", 
                timestamp, mailFrom, to, ex.getMessage());
            errorMessage = ex.getMessage();
            
            // Tentativo con fallback (support@funkard.com)
            try {
                sendUsing(fallbackMailSender, mailFallback, mailFromName, to, subject, bodyHtml, isHtml);
                log.info("✅ [{}] Email inviata da {} (fallback) a {} - Oggetto: {}", 
                    timestamp, mailFallback, to, subject);
                actualSender = mailFallback;
                success = true;
                errorMessage = null;
            } catch (Exception e) {
                log.error("❌ [{}] Errore invio anche da fallback {} a {}: {}", 
                    timestamp, mailFallback, to, e.getMessage(), e);
                errorMessage = "Primary: " + ex.getMessage() + "; Fallback: " + e.getMessage();
                
                // Alert interno se configurato
                if (adminEmail != null && !adminEmail.isEmpty()) {
                    log.error("🚨 [{}] ALERT: Invio email fallito per destinatario {} - Controllare configurazione SMTP", 
                        timestamp, to);
                }
            }
        }
        
        // Registra log email (nota: templateManager gestisce fallback internamente)
        // Qui registriamo solo il risultato finale
        if (success) {
            com.funkard.model.EmailLog emailLog = logService.logEmailSent(
                to, actualSender, subject, emailType, locale, templateName, false);
            return emailLog != null ? emailLog.getId() : null;
        } else {
            com.funkard.model.EmailLog emailLog = logService.logEmailFailed(
                to, actualSender, subject, emailType, locale, templateName, errorMessage);
            return emailLog != null ? emailLog.getId() : null;
        }
    }
    
    /**
     * 📧 Metodo generico per invio email (backward compatibility)
     */
    public boolean sendEmail(String to, String subject, String bodyHtml, boolean isHtml) {
        java.util.UUID logId = sendEmail(to, subject, bodyHtml, isHtml, "GENERIC", "it", null);
        return logId != null;
    }
    
    /**
     * 🔧 Metodo privato per invio email usando un JavaMailSender specifico
     * 
     * @param mailSender JavaMailSender da usare
     * @param fromEmail Email mittente
     * @param fromName Nome mittente
     * @param to Email destinatario
     * @param subject Oggetto email
     * @param bodyHtml Corpo email
     * @param isHtml true se HTML, false se testo
     * @throws MessagingException se invio fallisce
     */
    private void sendUsing(JavaMailSender mailSender, String fromEmail, String fromName, 
                          String to, String subject, String bodyHtml, boolean isHtml) 
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        try {
            helper.setFrom(fromEmail, fromName);
        } catch (java.io.UnsupportedEncodingException e) {
            // Fallback: usa solo email senza nome
            helper.setFrom(fromEmail);
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(bodyHtml, isHtml);
        mailSender.send(message);
    }

    /**
     * 📧 Invia email di verifica account con rilevamento automatico lingua
     * 
     * @param to Email destinatario
     * @param token Token verifica
     * @param userLocale Locale utente (da user.language, default: en)
     */
    public void sendVerificationEmail(String to, String token, String userLocale) {
        Locale locale = parseLocale(userLocale);
        sendVerificationEmail(to, token, locale);
    }
    
    /**
     * 📧 Invia email di verifica account
     */
    public void sendVerificationEmail(String to, String token) {
        sendVerificationEmail(to, token, Locale.ENGLISH);
    }
    
    /**
     * 📧 Invia email di verifica account
     */
    public void sendVerificationEmail(String to, String token, Locale locale) {
        String verifyUrl = "https://funkard.com/api/auth/verify?token=" + token;
        
        // Usa template con fallback multilingua
        Map<String, Object> variables = new HashMap<>();
        variables.put("verifyUrl", verifyUrl);
        variables.put("token", token);
        variables.put("userName", extractUsernameFromEmail(to));
        
        String subject = getSubjectForType("ACCOUNT_CONFIRMATION", locale);
        String content = templateManager.renderHtmlTemplate(
            "account_confirmation", variables, locale);
        
        String localeStr = locale != null ? locale.getLanguage() : "en";
        java.util.UUID logId = sendEmail(to, subject, content, true, 
            "ACCOUNT_CONFIRMATION", localeStr, "account_confirmation");
        if (logId == null) {
            throw new RuntimeException("Errore durante l'invio dell'email di verifica");
        }
    }
    
    /**
     * 🔍 Estrae username da email (parte prima di @)
     */
    private String extractUsernameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }
        return "User";
    }
    
    /**
     * 📧 Invia email usando template con rilevamento automatico lingua utente
     * 
     * @param to Email destinatario
     * @param emailType Tipo email (es. ACCOUNT_CONFIRMATION)
     * @param variables Variabili per template
     * @param userLocale Locale utente (da user.language o user.locale)
     */
    public void sendTemplatedEmail(String to, String emailType, Map<String, Object> variables, String userLocale) {
        Locale locale = parseLocale(userLocale);
        sendTemplatedEmail(to, emailType, variables, locale);
    }
    
    /**
     * 📧 Invia email usando template
     * 
     * @param to Email destinatario
     * @param emailType Tipo email (es. ACCOUNT_CONFIRMATION)
     * @param variables Variabili per template
     * @param locale Locale (it, en, es, de, fr, ecc.)
     */
    public void sendTemplatedEmail(String to, String emailType, Map<String, Object> variables, Locale locale) {
        // Determina subject e template name in base al tipo
        String templateName = emailType.toLowerCase().replace("_", "_");
        String subject = getSubjectForType(emailType, locale);
        
        // Renderizza template con fallback multilingua
        String htmlContent = templateManager.renderHtmlTemplate(templateName, variables, locale);
        String textContent = templateManager.renderTextTemplate(templateName, variables, locale);
        
        // Usa HTML come principale
        String localeStr = locale != null ? locale.getLanguage() : "en";
        java.util.UUID logId = sendEmail(to, subject, htmlContent, true, 
            emailType, localeStr, templateName);
        
        if (logId == null) {
            throw new RuntimeException("Errore durante l'invio dell'email templated: " + emailType);
        }
    }
    
    /**
     * 🌍 Parse stringa locale in Locale object
     */
    private Locale parseLocale(String localeStr) {
        if (localeStr == null || localeStr.isEmpty()) {
            return Locale.ENGLISH; // Default inglese
        }
        
        // Supporta formati: "it", "it-IT", "en-US", "es-419", ecc.
        String[] parts = localeStr.split("-");
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else if (parts.length == 2) {
            // Gestisci casi speciali
            if (parts[0].equals("es") && parts[1].equals("419")) {
                return new Locale("es", "419");
            }
            return new Locale(parts[0], parts[1]);
        }
        
        return Locale.ENGLISH; // Fallback
    }
    
    /**
     * 📝 Ottiene subject in base al tipo email e locale
     * Supporta 25+ lingue con fallback all'inglese
     */
    private String getSubjectForType(String emailType, Locale locale) {
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        
        String lang = locale.getLanguage().toLowerCase();
        
        // Mappa subject per tipo e lingua
        Map<String, Map<String, String>> subjects = getSubjectMap();
        
        Map<String, String> typeSubjects = subjects.get(emailType.toUpperCase());
        if (typeSubjects == null) {
            typeSubjects = subjects.get("DEFAULT");
        }
        
        // Cerca subject nella lingua richiesta
        String subject = typeSubjects.get(lang);
        if (subject == null) {
            // Fallback all'inglese
            subject = typeSubjects.get("en");
        }
        if (subject == null) {
            subject = "Notification from Funkard";
        }
        
        return subject;
    }
    
    /**
     * 📋 Mappa subject per tipo email e lingua
     */
    private Map<String, Map<String, String>> getSubjectMap() {
        Map<String, Map<String, String>> map = new HashMap<>();
        
        // ACCOUNT_CONFIRMATION
        Map<String, String> accountConfirmation = new HashMap<>();
        accountConfirmation.put("en", "Verify your Funkard account");
        accountConfirmation.put("it", "Verifica il tuo account Funkard");
        accountConfirmation.put("es", "Verifica tu cuenta de Funkard");
        accountConfirmation.put("de", "Bestätige dein Funkard-Konto");
        accountConfirmation.put("fr", "Vérifiez votre compte Funkard");
        accountConfirmation.put("pt", "Verifique sua conta Funkard");
        map.put("ACCOUNT_CONFIRMATION", accountConfirmation);
        
        // PASSWORD_RESET
        Map<String, String> passwordReset = new HashMap<>();
        passwordReset.put("en", "Reset your Funkard password");
        passwordReset.put("it", "Reset password Funkard");
        passwordReset.put("es", "Restablecer contraseña de Funkard");
        passwordReset.put("de", "Funkard-Passwort zurücksetzen");
        passwordReset.put("fr", "Réinitialiser votre mot de passe Funkard");
        passwordReset.put("pt", "Redefinir senha Funkard");
        map.put("PASSWORD_RESET", passwordReset);
        
        // ORDER_CONFIRMATION
        Map<String, String> orderConfirmation = new HashMap<>();
        orderConfirmation.put("en", "Order Confirmation - Funkard");
        orderConfirmation.put("it", "Conferma ordine - Funkard");
        orderConfirmation.put("es", "Confirmación de pedido - Funkard");
        orderConfirmation.put("de", "Bestellbestätigung - Funkard");
        orderConfirmation.put("fr", "Confirmation de commande - Funkard");
        orderConfirmation.put("pt", "Confirmação de pedido - Funkard");
        map.put("ORDER_CONFIRMATION", orderConfirmation);
        
        // ORDER_SHIPPED
        Map<String, String> orderShipped = new HashMap<>();
        orderShipped.put("en", "Your Funkard order has been shipped");
        orderShipped.put("it", "Il tuo ordine Funkard è stato spedito");
        orderShipped.put("es", "Tu pedido de Funkard ha sido enviado");
        orderShipped.put("de", "Deine Funkard-Bestellung wurde versendet");
        orderShipped.put("fr", "Votre commande Funkard a été expédiée");
        orderShipped.put("pt", "Seu pedido Funkard foi enviado");
        map.put("ORDER_SHIPPED", orderShipped);
        
        // ACCOUNT_DELETION
        Map<String, String> accountDeletion = new HashMap<>();
        accountDeletion.put("en", "Funkard — Account deletion completed");
        accountDeletion.put("it", "Funkard — Cancellazione account completata");
        accountDeletion.put("es", "Funkard — Eliminación de cuenta completada");
        accountDeletion.put("de", "Funkard — Kontolöschung abgeschlossen");
        accountDeletion.put("fr", "Funkard — Suppression de compte terminée");
        accountDeletion.put("pt", "Funkard — Exclusão de conta concluída");
        map.put("ACCOUNT_DELETION", accountDeletion);
        
        // TICKET_OPENED
        Map<String, String> ticketOpened = new HashMap<>();
        ticketOpened.put("en", "Support Ticket Opened - Funkard");
        ticketOpened.put("it", "Ticket supporto aperto - Funkard");
        ticketOpened.put("es", "Ticket de soporte abierto - Funkard");
        ticketOpened.put("de", "Support-Ticket eröffnet - Funkard");
        ticketOpened.put("fr", "Ticket de support ouvert - Funkard");
        ticketOpened.put("pt", "Ticket de suporte aberto - Funkard");
        map.put("TICKET_OPENED", ticketOpened);
        
        // DEFAULT
        Map<String, String> defaultSubject = new HashMap<>();
        defaultSubject.put("en", "Notification from Funkard");
        defaultSubject.put("it", "Notifica da Funkard");
        defaultSubject.put("es", "Notificación de Funkard");
        defaultSubject.put("de", "Benachrichtigung von Funkard");
        defaultSubject.put("fr", "Notification de Funkard");
        defaultSubject.put("pt", "Notificação da Funkard");
        map.put("DEFAULT", defaultSubject);
        
        return map;
    }

    /**
     * 📧 Invia email semplice (testo)
     */
    public void sendSimple(String to, String subject, String body) {
        boolean sent = sendEmail(to, subject, body, false);
        if (!sent) {
            throw new RuntimeException("Errore durante l'invio dell'email");
        }
    }
    
    /**
     * 📧 Invia email di conferma cancellazione account completata (GDPR Art. 17)
     * 
     * @param toEmail Email destinatario
     * @param locale Locale (IT o EN, default IT)
     * @param userDisplayName Nome utente (opzionale, può essere null)
     */
    public void sendAccountDeletionCompletedEmail(String toEmail, String locale, String userDisplayName) {
        log.info("📧 Preparazione email conferma cancellazione per: {} (locale: {})", toEmail, locale);
        
        // Determina locale (default IT)
        Locale emailLocale = determineLocale(locale);
        boolean isItalian = emailLocale.getLanguage().equals("it");
        
        // Template email
        String subject = isItalian 
            ? "Funkard — Cancellazione account completata"
            : "Funkard — Account deletion completed";
        
        // Usa template se disponibile
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userDisplayName != null ? userDisplayName : (isItalian ? "utente" : "user"));
        variables.put("date", LocalDateTime.now().format(
            DateTimeFormatter.ofPattern(isItalian ? "dd/MM/yyyy HH:mm:ss" : "yyyy-MM-dd HH:mm:ss")
        ));
        
        String htmlBody = templateManager.renderHtmlTemplate(
            "account_deletion", variables, emailLocale);
        
        // Fallback se template non disponibile
        if (htmlBody.contains("Template non disponibile")) {
            htmlBody = buildDeletionEmailHtmlBody(isItalian, userDisplayName);
        }
        
        java.util.UUID logId = sendEmail(toEmail, subject, htmlBody, true, 
            "ACCOUNT_DELETION", locale != null ? locale : "it", "account_deletion");
        if (logId == null) {
            log.error("❌ Errore durante invio email conferma cancellazione a {}", toEmail);
            throw new RuntimeException("Errore durante l'invio dell'email di conferma cancellazione");
        }
    }
    
    /**
     * 🔄 Invia email con retry (max 3 tentativi con backoff)
     * 
     * @param toEmail Email destinatario
     * @param locale Locale
     * @param userDisplayName Nome utente
     * @return true se invio riuscito, false altrimenti
     */
    public boolean sendAccountDeletionCompletedEmailWithRetry(
            String toEmail, 
            String locale, 
            String userDisplayName) {
        
        int maxRetries = 3;
        long backoffMs = 1000; // 1 secondo iniziale
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                sendAccountDeletionCompletedEmail(toEmail, locale, userDisplayName);
                return true;
            } catch (Exception e) {
                log.warn("Tentativo {} di {} fallito per email {}: {}", 
                    attempt, maxRetries, toEmail, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(backoffMs * attempt); // Backoff esponenziale: 1s, 2s, 3s
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Interruzione durante backoff retry");
                        return false;
                    }
                } else {
                    log.error("❌ Invio email fallito dopo {} tentativi per: {}", maxRetries, toEmail);
                    return false;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 🔍 Determina locale da stringa
     */
    private Locale determineLocale(String locale) {
        if (locale == null || locale.isEmpty()) {
            return Locale.ITALIAN;
        }
        
        String lowerLocale = locale.toLowerCase();
        if (lowerLocale.startsWith("en")) {
            return Locale.ENGLISH;
        }
        return Locale.ITALIAN; // Default IT
    }
    
    /**
     * 📝 Costruisce corpo email testuale
     */
    private String buildDeletionEmailBody(boolean isItalian, String userDisplayName) {
        String name = userDisplayName != null && !userDisplayName.isEmpty() 
            ? userDisplayName 
            : (isItalian ? "utente" : "user");
        
        String dateTime = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern(isItalian ? "dd/MM/yyyy HH:mm:ss" : "yyyy-MM-dd HH:mm:ss")
        );
        
        if (isItalian) {
            return String.format(
                "Ciao %s,\n\n" +
                "ti confermiamo che il tuo account Funkard e i tuoi dati personali sono stati cancellati definitivamente in data %s.\n\n" +
                "Questa operazione è irreversibile.\n\n" +
                "Se hai necessità di assistenza, contatta legal@funkard.com o support@funkard.com.\n\n" +
                "Grazie per aver utilizzato Funkard.\n\n" +
                "— Team Funkard",
                name, dateTime
            );
        } else {
            return String.format(
                "Hello %s,\n\n" +
                "we confirm that your Funkard account and personal data have been permanently deleted on %s.\n\n" +
                "This operation is irreversible.\n\n" +
                "If you need assistance, please contact legal@funkard.com or support@funkard.com.\n\n" +
                "Thank you for using Funkard.\n\n" +
                "— Funkard Team",
                name, dateTime
            );
        }
    }
    
    /**
     * 📝 Costruisce corpo email HTML
     */
    private String buildDeletionEmailHtmlBody(boolean isItalian, String userDisplayName) {
        String name = userDisplayName != null && !userDisplayName.isEmpty() 
            ? userDisplayName 
            : (isItalian ? "utente" : "user");
        
        String dateTime = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern(isItalian ? "dd/MM/yyyy HH:mm:ss" : "yyyy-MM-dd HH:mm:ss")
        );
        
        if (isItalian) {
            return String.format(
                "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #f2b237;'>Funkard — Cancellazione account completata</h2>" +
                "<p>Ciao %s,</p>" +
                "<p>ti confermiamo che il tuo account Funkard e i tuoi dati personali sono stati cancellati definitivamente in data <strong>%s</strong>.</p>" +
                "<p style='color: #d32f2f; font-weight: bold;'>Questa operazione è irreversibile.</p>" +
                "<p>Se hai necessità di assistenza, contatta:</p>" +
                "<ul>" +
                "<li><a href='mailto:legal@funkard.com'>legal@funkard.com</a></li>" +
                "<li><a href='mailto:support@funkard.com'>support@funkard.com</a></li>" +
                "</ul>" +
                "<p>Grazie per aver utilizzato Funkard.</p>" +
                "<p style='margin-top: 30px; border-top: 1px solid #ddd; padding-top: 20px; color: #666; font-size: 12px;'>" +
                "— Team Funkard<br>" +
                "Questa email è stata inviata automaticamente. Si prega di non rispondere.</p>" +
                "</div>" +
                "</body>" +
                "</html>",
                name, dateTime
            );
        } else {
            return String.format(
                "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #f2b237;'>Funkard — Account deletion completed</h2>" +
                "<p>Hello %s,</p>" +
                "<p>we confirm that your Funkard account and personal data have been permanently deleted on <strong>%s</strong>.</p>" +
                "<p style='color: #d32f2f; font-weight: bold;'>This operation is irreversible.</p>" +
                "<p>If you need assistance, please contact:</p>" +
                "<ul>" +
                "<li><a href='mailto:legal@funkard.com'>legal@funkard.com</a></li>" +
                "<li><a href='mailto:support@funkard.com'>support@funkard.com</a></li>" +
                "</ul>" +
                "<p>Thank you for using Funkard.</p>" +
                "<p style='margin-top: 30px; border-top: 1px solid #ddd; padding-top: 20px; color: #666; font-size: 12px;'>" +
                "— Funkard Team<br>" +
                "This email was sent automatically. Please do not reply.</p>" +
                "</div>" +
                "</body>" +
                "</html>",
                name, dateTime
            );
        }
    }
}