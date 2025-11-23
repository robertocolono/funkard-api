package com.funkard.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🤖 Servizio traduzione tramite OpenAI GPT
 * 
 * Utilizza OpenAI API per tradurre testi in diverse lingue.
 * Fallback automatico al testo originale in caso di errore.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiTranslateService implements TranslationProvider {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${openai.api.key:}")
    private String apiKey;
    
    @Value("${openai.api.model:gpt-4o-mini}")
    private String model;
    
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;
    
    /**
     * 🌍 Traduce testo in una lingua target usando OpenAI GPT
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
            throw new TranslationException("OPENAI_API_KEY non configurata");
        }
        
        try {
            // Costruisci prompt
            String prompt = String.format("Translate the following text to %s. Text: %s", 
                targetLanguage, text);
            
            // Prepara request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 1000);
            
            // Prepara headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // Chiamata API
            log.debug("🤖 Chiamata OpenAI API per traduzione: {} -> {}", 
                text.substring(0, Math.min(50, text.length())), targetLanguage);
            
            ResponseEntity<OpenAiResponse> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                request,
                OpenAiResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                OpenAiResponse body = response.getBody();
                if (body.getChoices() != null && !body.getChoices().isEmpty()) {
                    String translated = body.getChoices().get(0).getMessage().getContent();
                    if (translated != null && !translated.trim().isEmpty()) {
                        log.info("✅ Traduzione OpenAI completata: {} -> {} ({} caratteri)", 
                            targetLanguage, 
                            translated.length() > 50 ? translated.substring(0, 50) + "..." : translated, 
                            translated.length());
                        return translated.trim();
                    }
                }
            }
            
            throw new TranslationException("Risposta OpenAI non valida");
            
        } catch (RestClientException e) {
            log.error("❌ Errore durante chiamata OpenAI API: {}", e.getMessage());
            throw new TranslationException("Errore durante chiamata OpenAI API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Errore imprevisto durante traduzione: {}", e.getMessage());
            throw new TranslationException("Errore imprevisto durante traduzione: " + e.getMessage(), e);
        }
    }
    
    /**
     * 🤖 Esegue una chiamata GPT con prompt personalizzato
     * 
     * @param customPrompt Prompt personalizzato da inviare a GPT
     * @return Risposta di GPT
     * @throws TranslationException se la chiamata fallisce
     */
    public String executeWithCustomPrompt(String customPrompt) throws TranslationException {
        // Verifica API key
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new TranslationException("OPENAI_API_KEY non configurata");
        }
        
        if (customPrompt == null || customPrompt.trim().isEmpty()) {
            throw new TranslationException("Prompt personalizzato vuoto");
        }
        
        try {
            // Prepara request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", customPrompt)
            ));
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 500);
            
            // Prepara headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // Chiamata API
            log.debug("🤖 Chiamata OpenAI API con prompt personalizzato");
            
            ResponseEntity<OpenAiResponse> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                request,
                OpenAiResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                OpenAiResponse body = response.getBody();
                if (body.getChoices() != null && !body.getChoices().isEmpty()) {
                    String result = body.getChoices().get(0).getMessage().getContent();
                    if (result != null && !result.trim().isEmpty()) {
                        log.debug("✅ Risposta OpenAI con prompt personalizzato ricevuta");
                        return result.trim();
                    }
                }
            }
            
            throw new TranslationException("Risposta OpenAI non valida");
            
        } catch (RestClientException e) {
            log.error("❌ Errore durante chiamata OpenAI API: {}", e.getMessage());
            throw new TranslationException("Errore durante chiamata OpenAI API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Errore imprevisto durante chiamata OpenAI: {}", e.getMessage());
            throw new TranslationException("Errore imprevisto durante chiamata OpenAI: " + e.getMessage(), e);
        }
    }
    
    /**
     * 🔍 Verifica se OpenAI è disponibile
     */
    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * 📊 DTO per risposta OpenAI API
     */
    @Data
    private static class OpenAiResponse {
        private List<Choice> choices;
        
        @Data
        private static class Choice {
            private Message message;
        }
        
        @Data
        private static class Message {
            private String role;
            private String content;
        }
    }
}

