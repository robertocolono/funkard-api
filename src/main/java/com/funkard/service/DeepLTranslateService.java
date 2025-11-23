package com.funkard.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 🌍 Servizio traduzione tramite DeepL API
 * 
 * Utilizzato come fallback quando OpenAI GPT fallisce.
 * Restituisce null in caso di errore per permettere fallback successivo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeepLTranslateService implements TranslationProvider {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${deepl.api.key:}")
    private String apiKey;
    
    @Value("${deepl.api.url:https://api-free.deepl.com/v2/translate}")
    private String apiUrl;
    
    /**
     * 🌍 Traduce testo in una lingua target usando DeepL API
     * 
     * @param text Testo da tradurre
     * @param targetLanguage Lingua di destinazione (es. "es", "it", "fr", "en")
     * @return Testo tradotto
     * @throws TranslationException se la traduzione fallisce
     */
    @Override
    public String translate(String text, String targetLanguage) throws TranslationException {
        // Validazione input
        if (text == null || text.trim().isEmpty()) {
            throw new TranslationException("Testo vuoto per traduzione");
        }
        
        if (targetLanguage == null || targetLanguage.trim().isEmpty()) {
            throw new TranslationException("Lingua target non specificata");
        }
        
        // Verifica API key
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new TranslationException("DEEPL_API_KEY non configurata");
        }
        
        try {
            // Normalizza lingua target per DeepL (es. "it", "en", "es")
            // DeepL usa codici ISO 639-1 in maiuscolo per alcune lingue
            String normalizedTargetLang = normalizeLanguageForDeepL(targetLanguage);
            
            // Prepara form-data body
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("text", text);
            body.add("target_lang", normalizedTargetLang);
            // source_lang è opzionale - DeepL può auto-rilevare
            
            // Prepara headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "DeepL-Auth-Key " + apiKey);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            // Chiamata API
            log.debug("🌍 Chiamata DeepL API per traduzione: {} -> {}", 
                text.substring(0, Math.min(50, text.length())), normalizedTargetLang);
            
            ResponseEntity<DeepLResponse> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                request,
                DeepLResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DeepLResponse bodyResponse = response.getBody();
                if (bodyResponse.getTranslations() != null && !bodyResponse.getTranslations().isEmpty()) {
                    String translated = bodyResponse.getTranslations().get(0).getText();
                    if (translated != null && !translated.trim().isEmpty()) {
                        log.info("✅ DeepL traduzione completata: {} -> {} ({} caratteri)", 
                            normalizedTargetLang, 
                            translated.length() > 50 ? translated.substring(0, 50) + "..." : translated, 
                            translated.length());
                        return translated.trim();
                    }
                }
            }
            
            throw new TranslationException("Risposta DeepL API non valida");
            
        } catch (RestClientException e) {
            log.warn("⚠️ DeepL: Errore durante chiamata API: {}", e.getMessage());
            throw new TranslationException("Errore durante chiamata DeepL API: " + e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof TranslationException) {
                throw e;
            }
            log.warn("⚠️ DeepL: Errore imprevisto durante traduzione: {}", e.getMessage());
            throw new TranslationException("Errore imprevisto durante traduzione: " + e.getMessage(), e);
        }
    }
    
    /**
     * 🔍 Verifica se DeepL è disponibile
     */
    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * 🔍 Normalizza codice lingua per DeepL API
     * 
     * DeepL supporta codici ISO 639-1, alcuni in maiuscolo (es. EN, IT, ES, FR, DE, PT)
     * Converte codici comuni al formato corretto.
     */
    private String normalizeLanguageForDeepL(String lang) {
        if (lang == null || lang.trim().isEmpty()) {
            return "EN";
        }
        
        String normalized = lang.trim().toUpperCase();
        
        // Mappa codici comuni a formato DeepL
        // DeepL usa: EN, IT, ES, FR, DE, PT, PL, RU, JA, ZH, ecc.
        String[] supportedLangs = {
            "EN", "IT", "ES", "FR", "DE", "PT", "PL", "RU", "JA", "ZH",
            "NL", "SV", "DA", "FI", "EL", "CS", "RO", "HU", "BG", "SK",
            "SL", "ET", "LV", "LT", "MT", "GA", "HR", "SR"
        };
        
        // Se è già un codice valido, restituiscilo
        for (String supported : supportedLangs) {
            if (supported.equals(normalized)) {
                return normalized;
            }
        }
        
        // Se è un codice a 2 caratteri, prova a usarlo direttamente
        if (normalized.length() == 2) {
            return normalized;
        }
        
        // Se è un codice esteso (es. "en-US"), estrai solo la parte principale
        if (normalized.contains("-")) {
            return normalized.substring(0, 2);
        }
        
        // Default a inglese se non riconosciuto
        log.warn("⚠️ DeepL: Codice lingua non riconosciuto '{}', uso EN come default", lang);
        return "EN";
    }
    
    /**
     * 📊 DTO per risposta DeepL API
     */
    @Data
    private static class DeepLResponse {
        @JsonProperty("translations")
        private List<Translation> translations;
        
        @Data
        private static class Translation {
            @JsonProperty("text")
            private String text;
            
            @JsonProperty("detected_source_language")
            private String detectedSourceLanguage;
        }
    }
}

