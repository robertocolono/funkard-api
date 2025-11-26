package com.funkard.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 📦 DTO per prodotti del marketplace
 * 
 * Include campi originali + convertedPrice e convertedCurrency
 * calcolati in base alla valuta preferita dell'utente.
 */
@Data
public class ProductDTO {
    private Long id;
    private String name;
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
    
    private Double estimatedValue;
    private String userId;
    private String descriptionOriginal;
    private String descriptionLanguage;
    private String nameEn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

