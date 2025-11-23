package com.funkard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 🍪 DTO per preferenze cookie
 * 
 * Usato per:
 * - Request/Response API
 * - Sincronizzazione frontend/backend
 * - GDPR compliance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CookiePreferencesDTO {
    
    /**
     * 🍪 Accettazione generale cookie
     */
    @JsonProperty("cookiesAccepted")
    private Boolean cookiesAccepted;
    
    /**
     * 🍪 Preferenze cookie dettagliate
     * 
     * Esempio:
     * {
     *   "necessary": true,
     *   "analytics": false,
     *   "marketing": false,
     *   "functional": true
     * }
     */
    @JsonProperty("cookiesPreferences")
    private Map<String, Boolean> cookiesPreferences;
    
    /**
     * 🔒 Timestamp accettazione (solo in response)
     */
    @JsonProperty("cookiesAcceptedAt")
    private LocalDateTime cookiesAcceptedAt;
    
    /**
     * 📅 Timestamp ultimo aggiornamento (solo in response)
     */
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}

