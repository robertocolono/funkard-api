package com.funkard.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 👤 DTO per profilo utente
 * 
 * ✅ Dati essenziali per il frontend
 * ✅ Compatibile con User entity
 */
@Data
public class UserProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String username;
    private String preferredCurrency;
    private String language;
    private String theme;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    /**
     * 🌍 Descrizione profilo venditore originale (testo scritto dall'utente)
     * Salvata nella lingua originale per traduzione on-demand
     * Massimo 500 caratteri
     */
    @Size(max = 500, message = "La bio del venditore non può superare 500 caratteri")
    private String descriptionOriginal;
    
    /**
     * 🌍 Lingua originale della descrizione profilo (codice ISO 639-1, es. "it", "en", "es")
     */
    private String descriptionLanguage;
}
