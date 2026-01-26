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
    
    /**
     * 💱 Valuta del prezzo (opzionale, default: EUR)
     * Codice ISO 4217 (es. EUR, USD, GBP, JPY, BRL, CAD, AUD)
     */
    private String currency;
    
    private String condition;
    
    private String cardId;
    
    /**
     * 📂 Categoria prodotto (TCG, SPORT, ENTERTAINMENT, VINTAGE)
     * Obbligatorio per creazione listing dalla Sell
     */
    @NotNull(message = "La categoria è obbligatoria")
    @NotBlank(message = "La categoria non può essere vuota")
    private String category;
    
    /**
     * 📝 Nome della carta (obbligatorio)
     */
    @NotNull(message = "Il nome della carta è obbligatorio")
    @NotBlank(message = "Il nome della carta non può essere vuoto")
    private String cardName;
    
    /**
     * 📚 Serie/Espansione (opzionale)
     */
    private String series;
    
    // Valori TCG, Lingua e Franchise
    private String tcg; // Valore TCG selezionato
    private String language; // Valore Lingua selezionato
    private String franchise; // Valore Franchise selezionato
    
    // Valori personalizzati "Altro" (opzionali)
    private String customTcg; // Valore personalizzato se tcg = "Altro"
    private String customLanguage; // Valore personalizzato se language = "Altro"
    private String customFranchise; // Valore personalizzato se franchise = "Altro"
}

