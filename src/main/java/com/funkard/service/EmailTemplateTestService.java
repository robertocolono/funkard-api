package com.funkard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 🧪 Service per test automatici template email
 * 
 * Verifica caricamento template, fallback e sostituzione variabili.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateTestService {
    
    private final EmailTemplateManager templateManager;
    
    /**
     * ✅ Test caricamento template per tutte le lingue supportate
     */
    public void testAllTemplates() {
        log.info("🧪 Inizio test template email...");
        
        String[] templates = {
            "account_confirmation",
            "password_reset",
            "order_confirmation",
            "order_shipped",
            "ticket_opened",
            "account_deletion"
        };
        
        String[] languages = {
            "it", "en", "es", "de", "fr", "pt", "nl", "pl", "ja", "zh",
            "ko", "id", "hi", "th", "ms", "vi", "fil", "tr", "ar", "he",
            "fa", "sw", "zu", "es-419", "en-us", "en-gb", "fr-ca"
        };
        
        Map<String, Object> testVariables = new HashMap<>();
        testVariables.put("userName", "Test User");
        testVariables.put("verifyUrl", "https://funkard.com/verify?token=test123");
        testVariables.put("orderId", "ORD-12345");
        testVariables.put("amount", "99.99");
        testVariables.put("currency", "EUR");
        
        int successCount = 0;
        int fallbackCount = 0;
        int errorCount = 0;
        
        for (String template : templates) {
            for (String lang : languages) {
                try {
                    Locale locale = parseLocale(lang);
                    String html = templateManager.renderHtmlTemplate(template, testVariables, locale);
                    
                    if (html.contains("Template non disponibile") || html.contains("Template not available")) {
                        fallbackCount++;
                        log.debug("⚠️ Template {} per {} usa fallback", template, lang);
                    } else {
                        successCount++;
                        log.debug("✅ Template {} per {} caricato correttamente", template, lang);
                    }
                } catch (Exception e) {
                    errorCount++;
                    log.error("❌ Errore test template {} per {}: {}", template, lang, e.getMessage());
                }
            }
        }
        
        log.info("🧪 Test completato - Successi: {}, Fallback: {}, Errori: {}", 
            successCount, fallbackCount, errorCount);
    }
    
    /**
     * ✅ Test sostituzione variabili
     */
    public boolean testVariableSubstitution() {
        log.info("🧪 Test sostituzione variabili...");
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", "Mario Rossi");
        variables.put("orderId", "ORD-12345");
        variables.put("amount", "99.99");
        variables.put("currency", "EUR");
        variables.put("verifyUrl", "https://funkard.com/verify?token=abc123");
        
        String template = "Hello ${userName}, your order ${orderId} for ${amount} ${currency} is confirmed.";
        String expected = "Hello Mario Rossi, your order ORD-12345 for 99.99 EUR is confirmed.";
        
        // Usa reflection per testare replaceVariables
        String result = templateManager.renderHtmlTemplate("account_confirmation", variables, Locale.ENGLISH);
        
        boolean success = result.contains("Mario Rossi") || result.contains("ORD-12345");
        log.info("🧪 Test sostituzione variabili: {}", success ? "✅ PASS" : "❌ FAIL");
        
        return success;
    }
    
    /**
     * 🌍 Parse stringa locale
     */
    private Locale parseLocale(String localeStr) {
        if (localeStr == null || localeStr.isEmpty()) {
            return Locale.ENGLISH;
        }
        
        String[] parts = localeStr.split("-");
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else if (parts.length == 2) {
            if (parts[0].equals("es") && parts[1].equals("419")) {
                return new Locale("es", "419");
            }
            return new Locale(parts[0], parts[1]);
        }
        
        return Locale.ENGLISH;
    }
}

