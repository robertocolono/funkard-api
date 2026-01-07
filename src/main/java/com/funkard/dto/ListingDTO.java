package com.funkard.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ListingDTO {
    private String id;
    private String title;
    private String description;
    private Double price;
    private String currency;
    
    /**
     * 💱 Prezzo convertito nella valuta preferita dell'utente
     */
    private Double convertedPrice;
    
    /**
     * 💱 Valuta del prezzo convertito
     */
    private String convertedCurrency;
    
    private String status;
    private LocalDateTime createdAt;
    private String sellerId;
    private String cardId;
    
    /**
     * 📂 Categoria della Card associata (TCG, SPORT, ENTERTAINMENT, VINTAGE)
     */
    private String category;
    
    /**
     * 📦 Tipo della Card associata (SINGLE_CARD, SEALED_BOX, BOOSTER_PACK, CASE, BOX, STARTER_DECK, COMPLETE_SET, PROMO, ACCESSORY)
     * 
     * Note: SEALED_BOX è mantenuto come legacy per retrocompatibilità.
     */
    private String type;
    
    /**
     * 📋 Condizione della carta (RAW, MINT, NEAR_MINT, EXCELLENT, VERY_GOOD, GOOD, FAIR, POOR)
     * 
     * Note: Campo nullable. Valore arriva direttamente dal database senza trasformazioni.
     */
    private String condition;
}