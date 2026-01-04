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
     * 📦 Tipo della Card associata (SINGLE_CARD, SEALED_BOX, BOOSTER_PACK, STARTER_DECK, COMPLETE_SET, PROMO, ACCESSORY)
     */
    private String type;
}