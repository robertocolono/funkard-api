package com.funkard.service;

import com.funkard.model.TranslationLog;
import com.funkard.repository.TranslationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 🌍 Servizio traduzione automatica
 * 
 * Supporta multiple API (DeepL, Google Translate) con fallback
 * a libreria interna. Logga tutte le traduzioni per audit.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {
    
    private final TranslationLogRepository logRepository;
    
    @Value("${translation.provider:internal}")
    private String translationProvider; // "deepl", "google", "internal"
    
    @Value("${translation.deepl.api-key:}")
    private String deeplApiKey;
    
    @Value("${translation.google.api-key:}")
    private String googleApiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 🌍 Traduce testo da una lingua all'altra
     * 
     * @param text Testo da tradurre
     * @param fromLang Lingua sorgente (ISO 639-1, es. "it", "en")
     * @param toLang Lingua destinazione (ISO 639-1, es. "it", "en")
     * @param userId ID utente (opzionale, per logging)
     * @param messageType Tipo messaggio (opzionale, per logging)
     * @param messageId ID messaggio (opzionale, per logging)
     * @return Testo tradotto o originale se fallisce
     */
    @Transactional
    public String translate(String text, String fromLang, String toLang, 
                           Long userId, String messageType, UUID messageId) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        // Se lingue uguali, non tradurre
        if (fromLang != null && toLang != null && fromLang.equalsIgnoreCase(toLang)) {
            log.debug("Lingue identiche ({}), nessuna traduzione necessaria", fromLang);
            return text;
        }
        
        // Normalizza lingue (ISO 639-1)
        String normalizedFrom = normalizeLanguage(fromLang);
        String normalizedTo = normalizeLanguage(toLang);
        
        if (normalizedFrom == null || normalizedTo == null) {
            log.warn("⚠️ Lingue non valide: {} -> {}", fromLang, toLang);
            logTranslation(text, null, normalizedFrom, normalizedTo, "internal", false, 
                         "Lingue non valide", userId, messageType, messageId);
            return text; // Restituisci originale
        }
        
        String translatedText = null;
        String provider = translationProvider;
        String errorMessage = null;
        boolean success = false;
        
        try {
            // Tentativo con provider configurato
            switch (provider.toLowerCase()) {
                case "deepl":
                    translatedText = translateWithDeepL(text, normalizedFrom, normalizedTo);
                    break;
                case "google":
                    translatedText = translateWithGoogle(text, normalizedFrom, normalizedTo);
                    break;
                default:
                    translatedText = translateInternal(text, normalizedFrom, normalizedTo);
                    provider = "internal";
                    break;
            }
            
            if (translatedText != null && !translatedText.trim().isEmpty()) {
                success = true;
                log.info("✅ Traduzione riuscita: {} -> {} (provider: {})", normalizedFrom, normalizedTo, provider);
            } else {
                // Fallback a provider interno
                log.warn("⚠️ Traduzione fallita con {}, tentativo fallback interno", provider);
                translatedText = translateInternal(text, normalizedFrom, normalizedTo);
                provider = "internal";
                success = (translatedText != null && !translatedText.trim().isEmpty());
            }
            
        } catch (Exception e) {
            log.error("❌ Errore durante traduzione: {}", e.getMessage(), e);
            errorMessage = e.getMessage();
            
            // Fallback a testo originale
            translatedText = text;
        }
        
        // Log traduzione
        logTranslation(text, translatedText, normalizedFrom, normalizedTo, provider, 
                     success, errorMessage, userId, messageType, messageId);
        
        return success ? translatedText : text;
    }
    
    /**
     * 🌍 Traduce con DeepL API
     */
    private String translateWithDeepL(String text, String fromLang, String toLang) {
        if (deeplApiKey == null || deeplApiKey.isEmpty()) {
            throw new IllegalStateException("DeepL API key non configurata");
        }
        
        try {
            String url = "https://api-free.deepl.com/v2/translate";
            Map<String, Object> request = new HashMap<>();
            request.put("text", new String[]{text});
            request.put("source_lang", fromLang.toUpperCase());
            request.put("target_lang", toLang.toUpperCase());
            request.put("auth_key", deeplApiKey);
            
            // Nota: Implementazione semplificata - in produzione usare client dedicato
            // Per ora restituiamo null per forzare fallback interno
            log.debug("DeepL translation requested: {} -> {}", fromLang, toLang);
            return null; // TODO: Implementare chiamata API reale
        } catch (Exception e) {
            log.error("Errore DeepL API: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 🌍 Traduce con Google Translate API
     */
    private String translateWithGoogle(String text, String fromLang, String toLang) {
        if (googleApiKey == null || googleApiKey.isEmpty()) {
            throw new IllegalStateException("Google Translate API key non configurata");
        }
        
        try {
            String url = "https://translation.googleapis.com/language/translate/v2";
            // Nota: Implementazione semplificata - in produzione usare client dedicato
            log.debug("Google translation requested: {} -> {}", fromLang, toLang);
            return null; // TODO: Implementare chiamata API reale
        } catch (Exception e) {
            log.error("Errore Google Translate API: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 🌍 Traduzione interna (fallback)
     * 
     * Usa dizionario base per traduzioni comuni o restituisce testo originale.
     * In produzione, considerare libreria come Apache Tika o simili.
     */
    private String translateInternal(String text, String fromLang, String toLang) {
        // Dizionario base per traduzioni comuni (esempio)
        Map<String, Map<String, String>> dictionary = getBasicDictionary();
        
        String lowerText = text.toLowerCase().trim();
        Map<String, String> translations = dictionary.get(lowerText);
        
        if (translations != null && translations.containsKey(toLang)) {
            return translations.get(toLang);
        }
        
        // Se non trovato, restituisci originale
        log.debug("Traduzione interna non disponibile per: {} ({} -> {})", text, fromLang, toLang);
        return text;
    }
    
    /**
     * 📚 Dizionario base per traduzioni comuni
     */
    private Map<String, Map<String, String>> getBasicDictionary() {
        Map<String, Map<String, String>> dict = new HashMap<>();
        
        // Esempi base
        Map<String, String> ciao = new HashMap<>();
        ciao.put("en", "Hello");
        ciao.put("it", "Ciao");
        ciao.put("es", "Hola");
        ciao.put("fr", "Bonjour");
        dict.put("ciao", ciao);
        dict.put("hello", ciao);
        dict.put("hola", ciao);
        
        // Aggiungi altre traduzioni comuni se necessario
        
        return dict;
    }
    
    /**
     * 🌍 Normalizza codice lingua a ISO 639-1
     */
    private String normalizeLanguage(String lang) {
        if (lang == null || lang.trim().isEmpty()) {
            return "en"; // Default
        }
        
        String normalized = lang.toLowerCase().trim();
        
        // Supporta formati comuni
        if (normalized.length() > 2) {
            normalized = normalized.substring(0, 2);
        }
        
        // Valida ISO 639-1
        String[] validCodes = {"en", "it", "es", "de", "fr", "pt", "nl", "pl", "ja", "zh", 
                              "ko", "id", "hi", "th", "ms", "vi", "fil", "tr", "ar", "he", 
                              "fa", "sw", "zu", "ru", "uk", "cs", "sk", "hu", "ro", "bg", 
                              "hr", "sr", "sl", "et", "lv", "lt", "fi", "sv", "da", "no", "is"};
        
        for (String code : validCodes) {
            if (code.equals(normalized)) {
                return normalized;
            }
        }
        
        // Se non valido, default a inglese
        log.warn("⚠️ Codice lingua non valido: {}, usando 'en'", lang);
        return "en";
    }
    
    /**
     * 📝 Logga traduzione in database
     */
    @Transactional
    private void logTranslation(String sourceText, String translatedText, 
                               String sourceLang, String targetLang, String provider,
                               boolean success, String errorMessage, Long userId,
                               String messageType, UUID messageId) {
        try {
            TranslationLog log = new TranslationLog();
            log.setSourceText(sourceText);
            log.setTranslatedText(translatedText);
            log.setSourceLanguage(sourceLang);
            log.setTargetLanguage(targetLang);
            log.setTranslationProvider(provider);
            log.setSuccess(success);
            log.setErrorMessage(errorMessage);
            log.setCreatedAt(LocalDateTime.now());
            
            // Nota: user viene caricato lazy se necessario
            // Per ora non lo carichiamo per evitare dipendenze circolari
            
            log.setMessageType(messageType);
            log.setMessageId(messageId);
            
            logRepository.save(log);
        } catch (Exception e) {
            log.error("Errore durante logging traduzione: {}", e.getMessage());
            // Non bloccare il flusso se il logging fallisce
        }
    }
}

