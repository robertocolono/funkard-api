package com.funkard.dto;

import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
    @DecimalMin(value = "0.01", message = "Il prezzo deve essere maggiore di zero")
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
    
    /**
     * 📦 Tipo prodotto (SINGLE_CARD, SEALED_BOX, BOOSTER_PACK, CASE, BOX, STARTER_DECK, COMPLETE_SET, PROMO, ACCESSORY)
     * Obbligatorio per creazione listing dalla Sell
     */
    @NotNull(message = "Il tipo è obbligatorio")
    @NotBlank(message = "Il tipo non può essere vuoto")
    private String type;
    
    // Valori TCG, Lingua e Franchise
    private String tcg; // Valore TCG selezionato
    private String language; // Valore Lingua selezionato
    private String franchise; // Valore Franchise selezionato
    
    // Valori personalizzati "Altro" (opzionali)
    private String customTcg; // Valore personalizzato se tcg = "Altro"
    private String customLanguage; // Valore personalizzato se language = "Altro"
    private String customFranchise; // Valore personalizzato se franchise = "Altro"
    
    /**
     * 📋 Dichiarazioni del venditore (JSON string)
     * Struttura: { provenance, authenticity, dating, finalDeclaration }
     * Opzionale, nessuna validazione per ora
     */
    private String sellerDeclarations;
    
    /**
     * 📦 Quantità disponibile per la vendita (obbligatoria)
     */
    @NotNull(message = "La quantità è obbligatoria")
    @Min(value = 1, message = "La quantità deve essere almeno 1")
    private Integer quantity;
    
    /**
     * 💰 Prezzo originale/acquisto (opzionale)
     * Se presente, deve essere >= 0
     */
    @DecimalMin(value = "0.00", message = "Il prezzo originale non può essere negativo")
    private BigDecimal originalPrice;
    
    /**
     * 🔄 Indica se questa vendita accetta scambi (opzionale)
     */
    private Boolean acceptTrades;
}

