package com.funkard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 📝 DTO per richiesta traduzione
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslateRequest {
    
    @NotBlank(message = "Il testo da tradurre è obbligatorio")
    private String text;
    
    @NotBlank(message = "La lingua di destinazione è obbligatoria")
    private String targetLanguage; // es. "es", "it", "fr", "en"
}

