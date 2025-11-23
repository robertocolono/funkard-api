package com.funkard.controller;

import com.funkard.dto.TranslateRequest;
import com.funkard.dto.TranslateResponse;
import com.funkard.service.UnifiedTranslationService;
import com.funkard.api.i18n.SupportedLanguages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Set;
import java.util.Map;

/**
 * 🌍 Controller per traduzione automatica unificata
 * 
 * Endpoint pubblico per tradurre testi in diverse lingue.
 * Utilizza GPT-4o-mini come provider primario e DeepL come fallback.
 */
@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
}, allowCredentials = "true")
public class TranslateController {
    
    private final UnifiedTranslationService translationService;
    
    /**
     * 🌍 Lingue supportate da Funkard
     * Utilizza la whitelist centralizzata SupportedLanguages.ALL
     */
    private static final Set<String> SUPPORTED_LANGUAGES = SupportedLanguages.ALL;
    
    /**
     * 🌍 POST /api/translate
     * Traduce un testo in una lingua target usando sistema unificato (GPT + DeepL fallback)
     * 
     * Request:
     * {
     *   "text": "Hello, how are you?",
     *   "targetLanguage": "it"
     * }
     * 
     * Response:
     * {
     *   "translated": "Ciao, come stai?"
     * }
     * 
     * @param request Richiesta con testo e lingua target
     * @return Testo tradotto
     */
    @PostMapping
    public ResponseEntity<?> translate(@Valid @RequestBody TranslateRequest request) {
        log.info("🌍 Richiesta traduzione: {} -> {} ({} caratteri)", 
            request.getTargetLanguage(), 
            request.getText().length() > 50 ? request.getText().substring(0, 50) + "..." : request.getText(),
            request.getText().length());
        
        try {
            // Validazione testo non vuoto
            if (request.getText() == null || request.getText().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Il testo da tradurre non può essere vuoto"));
            }
            
            // Normalizza lingua target (rimuovi spazi, lowercase)
            String normalizedLanguage = request.getTargetLanguage().trim().toLowerCase();
            
            // Estrai codice principale se formato esteso (es. "en-US" → "en")
            if (normalizedLanguage.contains("-")) {
                normalizedLanguage = normalizedLanguage.substring(0, normalizedLanguage.indexOf("-"));
            }
            
            // Limita a 2 caratteri (ISO 639-1)
            if (normalizedLanguage.length() > 2) {
                normalizedLanguage = normalizedLanguage.substring(0, 2);
            }
            
            // Valida lingua supportata
            if (!SUPPORTED_LANGUAGES.contains(normalizedLanguage)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", 
                        String.format("Lingua non supportata: '%s'. Lingue supportate: %s", 
                            request.getTargetLanguage(), String.join(", ", SupportedLanguages.ORDERED))));
            }
            
            // Esegui traduzione con sistema unificato
            String translated = translationService.translate(request.getText(), normalizedLanguage);
            
            TranslateResponse response = new TranslateResponse(translated);
            
            log.info("✅ Traduzione completata con successo");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Richiesta non valida: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Richiesta non valida: " + e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Errore durante traduzione: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server durante traduzione"));
        }
    }
}

