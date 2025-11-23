package com.funkard.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 📝 Request DTO per creazione listing/vendita
 * 
 * Supporta valori personalizzati "Altro" per TCG e Lingua.
 */
@Data
public class CreateListingRequest {
    
    @NotBlank(message = "Il titolo è obbligatorio")
    private String title;
    
    private String description;
    
    @NotNull(message = "Il prezzo è obbligatorio")
    private BigDecimal price;
    
    private String condition;
    
    private String cardId;
    
    // Valori TCG, Lingua e Franchise
    private String tcg; // Valore TCG selezionato
    private String language; // Valore Lingua selezionato
    private String franchise; // Valore Franchise selezionato
    
    // Valori personalizzati "Altro" (opzionali)
    private String customTcg; // Valore personalizzato se tcg = "Altro"
    private String customLanguage; // Valore personalizzato se language = "Altro"
    private String customFranchise; // Valore personalizzato se franchise = "Altro"
}

