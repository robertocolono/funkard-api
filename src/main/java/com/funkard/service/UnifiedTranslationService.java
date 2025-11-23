package com.funkard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 🌍 Servizio di traduzione unificato
 * 
 * Gestisce traduzioni con fallback automatico:
 * 1. Prova GPT-4o-mini (provider primario)
 * 2. Se fallisce → prova DeepL (provider secondario)
 * 3. Se fallisce anche DeepL → restituisce testo originale
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedTranslationService {
    
    private final OpenAiTranslateService openAiProvider;
    private final DeepLTranslateService deepLProvider;
    
    /**
     * 🌍 Traduce un testo in una lingua target
     * 
     * Flusso:
     * 1. Prova GPT-4o-mini
     * 2. Se fallisce → prova DeepL
     * 3. Se fallisce anche DeepL → restituisce testo originale
     * 
     * @param text Testo da tradurre
     * @param targetLanguage Lingua di destinazione (codice ISO 639-1, es. "it", "en", "es")
     * @return Testo tradotto o testo originale se entrambi i provider falliscono
     */
    public String translate(String text, String targetLanguage) {
        // Validazione input
        if (text == null || text.trim().isEmpty()) {
            log.warn("⚠️ Testo vuoto per traduzione");
            return text;
        }
        
        if (targetLanguage == null || targetLanguage.trim().isEmpty()) {
            log.warn("⚠️ Lingua target non specificata, restituisco testo originale");
            return text;
        }
        
        // Normalizza lingua target
        String normalizedLang = normalizeLanguage(targetLanguage);
        
        // 1. Prova GPT-4o-mini (provider primario)
        if (openAiProvider.isAvailable()) {
            try {
                String translated = openAiProvider.translate(text, normalizedLang);
                log.info("✅ Traduzione completata con GPT: {} ({} caratteri)", 
                    normalizedLang, translated.length());
                return translated;
            } catch (TranslationException e) {
                log.warn("⚠️ GPT fallito: {}, provo fallback DeepL", e.getMessage());
            } catch (Exception e) {
                log.warn("⚠️ Errore imprevisto GPT: {}, provo fallback DeepL", e.getMessage());
            }
        } else {
            log.warn("⚠️ GPT non disponibile (API key non configurata), provo DeepL");
        }
        
        // 2. Fallback a DeepL (provider secondario)
        if (deepLProvider.isAvailable()) {
            try {
                String translated = deepLProvider.translate(text, normalizedLang);
                log.info("✅ Traduzione completata con DeepL (fallback): {} ({} caratteri)", 
                    normalizedLang, translated.length());
                return translated;
            } catch (TranslationException e) {
                log.warn("⚠️ DeepL fallito: {}", e.getMessage());
            } catch (Exception e) {
                log.warn("⚠️ Errore imprevisto DeepL: {}", e.getMessage());
            }
        } else {
            log.warn("⚠️ DeepL non disponibile (API key non configurata)");
        }
        
        // 3. Fallback finale: restituisce testo originale
        log.error("❌ Entrambi i provider di traduzione hanno fallito, restituisco testo originale");
        return text;
    }
    
    /**
     * 🔍 Normalizza codice lingua (ISO 639-1)
     * 
     * Supporta formati comuni e estrae solo il codice principale (es. "en-US" → "en")
     */
    private String normalizeLanguage(String lang) {
        if (lang == null || lang.trim().isEmpty()) {
            return "en";
        }
        
        String normalized = lang.trim().toLowerCase();
        
        // Se è un codice esteso (es. "en-US", "pt-BR"), estrai solo la parte principale
        if (normalized.contains("-")) {
            normalized = normalized.substring(0, normalized.indexOf("-"));
        }
        
        // Limita a 2 caratteri (ISO 639-1)
        if (normalized.length() > 2) {
            normalized = normalized.substring(0, 2);
        }
        
        return normalized;
    }
}

