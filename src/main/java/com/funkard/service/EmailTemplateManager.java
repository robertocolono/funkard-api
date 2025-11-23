package com.funkard.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 📄 Template Manager per email multilingua
 * 
 * Gestisce template email in 25+ lingue con fallback automatico all'inglese.
 * Supporta sostituzione variabili dinamiche e logging completo.
 */
@Service
@Slf4j
public class EmailTemplateManager {
    
    private static final String TEMPLATE_BASE_PATH = "email-templates/";
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    // Lingue supportate con fallback
    private static final String[] SUPPORTED_LANGUAGES = {
        "it", "en", "es", "de", "fr", "pt", "nl", "pl", "ja", "zh",
        "ko", "id", "hi", "th", "ms", "vi", "fil", "tr", "ar", "he",
        "fa", "sw", "zu", "es-419", "en-us", "en-gb", "fr-ca"
    };
    
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String PLACEHOLDER_FILE = "_placeholder.txt";
    
    /**
     * 📄 Renderizza template email con fallback multilingua
     * 
     * @param templateName Nome template (es. "account_confirmation")
     * @param variables Variabili da sostituire
     * @param locale Locale utente
     * @param isHtml true per HTML, false per testo
     * @return Template renderizzato con variabili sostituite
     */
    public String renderTemplate(String templateName, Map<String, Object> variables, Locale locale, boolean isHtml) {
        String extension = isHtml ? ".html" : ".txt";
        String language = normalizeLanguage(locale);
        boolean usedFallback = false;
        
        // Tentativo 1: Template nella lingua specifica
        String templatePath = TEMPLATE_BASE_PATH + language + "/" + templateName + extension;
        String template = loadTemplate(templatePath);
        String requestedLanguage = language;
        
        if (template == null && !language.equals(DEFAULT_LANGUAGE)) {
            // Tentativo 2: Fallback all'inglese
            log.warn("⚠️ Template {} non trovato per lingua {}, fallback a {}", templateName, language, DEFAULT_LANGUAGE);
            templatePath = TEMPLATE_BASE_PATH + DEFAULT_LANGUAGE + "/" + templateName + extension;
            template = loadTemplate(templatePath);
            usedFallback = true;
            language = DEFAULT_LANGUAGE;
        }
        
        if (template == null) {
            // Tentativo 3: Template generico senza lingua
            log.warn("⚠️ Template {} non trovato nemmeno in inglese, uso template generico", templateName);
            template = generateFallbackTemplate(templateName, variables, isHtml);
            usedFallback = true;
        }
        
        // Sostituisce variabili
        String rendered = replaceVariables(template, variables);
        
        // Log se usato fallback
        if (usedFallback) {
            logToFile("Template fallback usato: {} - Lingua richiesta: {} - Lingua usata: {}", 
                templateName, locale != null ? locale.getLanguage() : "null", language);
        }
        
        return rendered;
    }
    
    /**
     * 🌍 Normalizza lingua da Locale a codice supportato
     */
    private String normalizeLanguage(Locale locale) {
        if (locale == null) {
            return DEFAULT_LANGUAGE;
        }
        
        String lang = locale.getLanguage().toLowerCase();
        String country = locale.getCountry().toLowerCase();
        
        // Casi speciali per varianti regionali
        if (lang.equals("es") && country.equals("419")) {
            return "es-419";
        }
        if (lang.equals("en")) {
            if (country.equals("us")) return "en-us";
            if (country.equals("gb")) return "en-gb";
        }
        if (lang.equals("fr") && country.equals("ca")) {
            return "fr-ca";
        }
        
        // Verifica se lingua è supportata
        for (String supported : SUPPORTED_LANGUAGES) {
            if (supported.equals(lang) || supported.startsWith(lang + "-")) {
                return supported;
            }
        }
        
        // Fallback a inglese se non supportata
        return DEFAULT_LANGUAGE;
    }
    
    /**
     * 📂 Carica template da file system
     */
    private String loadTemplate(String templatePath) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            if (resource.exists()) {
                return StreamUtils.copyToString(
                    resource.getInputStream(),
                    StandardCharsets.UTF_8
                );
            }
        } catch (IOException e) {
            log.debug("Template non trovato: {} - {}", templatePath, e.getMessage());
        }
        return null;
    }
    
    /**
     * 🔄 Sostituisce variabili nel template
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        
        // Aggiungi variabili di sistema
        variables.putIfAbsent("brandName", "Funkard");
        variables.putIfAbsent("supportEmail", "support@funkard.com");
        variables.putIfAbsent("legalEmail", "legal@funkard.com");
        
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = variables.get(varName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 🔄 Genera template fallback se file non trovato
     */
    private String generateFallbackTemplate(String templateName, Map<String, Object> variables, boolean isHtml) {
        log.error("❌ Template {} non trovato, generazione fallback", templateName);
        logToFile("ERROR: Template mancante - {}", templateName);
        
        if (isHtml) {
            return String.format(
                "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px;'>" +
                "<h1>%s</h1>" +
                "<p>Template not available. Please contact support@funkard.com</p>" +
                "<p>Template: %s</p>" +
                "</body></html>",
                variables.getOrDefault("brandName", "Funkard"), templateName
            );
        } else {
            return String.format(
                "%s\n\nTemplate not available: %s\nPlease contact support@funkard.com",
                variables.getOrDefault("brandName", "Funkard"), templateName
            );
        }
    }
    
    /**
     * 📝 Logga in file email.log
     */
    private void logToFile(String message, Object... args) {
        try {
            String logMessage = String.format(message, args);
            String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );
            String logEntry = String.format("[%s] %s%n", timestamp, logMessage);
            
            // Log su file (se permessi) o solo su logger
            log.warn("📧 EMAIL_TEMPLATE: {}", logEntry.trim());
        } catch (Exception e) {
            log.error("Errore logging template: {}", e.getMessage());
        }
    }
    
    /**
     * 📄 Renderizza template HTML
     */
    public String renderHtmlTemplate(String templateName, Map<String, Object> variables, Locale locale) {
        return renderTemplate(templateName, variables, locale, true);
    }
    
    /**
     * 📄 Renderizza template testo
     */
    public String renderTextTemplate(String templateName, Map<String, Object> variables, Locale locale) {
        return renderTemplate(templateName, variables, locale, false);
    }
    
    /**
     * ✅ Verifica se template esiste per una lingua
     */
    public boolean templateExists(String templateName, Locale locale, boolean isHtml) {
        String extension = isHtml ? ".html" : ".txt";
        String language = normalizeLanguage(locale);
        String templatePath = TEMPLATE_BASE_PATH + language + "/" + templateName + extension;
        
        ClassPathResource resource = new ClassPathResource(templatePath);
        return resource.exists();
    }
    
    /**
     * 📋 Lista template disponibili per una lingua
     */
    public java.util.List<String> listAvailableTemplates(Locale locale, boolean isHtml) {
        String extension = isHtml ? ".html" : ".txt";
        String language = normalizeLanguage(locale);
        String templateDir = TEMPLATE_BASE_PATH + language + "/";
        
        java.util.List<String> templates = new java.util.ArrayList<>();
        try {
            ClassPathResource dirResource = new ClassPathResource(templateDir);
            if (dirResource.exists()) {
                // Nota: Spring Resource non supporta listing diretto
                // In produzione, usa FileSystemResource o implementa listing custom
            }
        } catch (Exception e) {
            log.debug("Errore listing template per {}: {}", language, e.getMessage());
        }
        return templates;
    }
}
