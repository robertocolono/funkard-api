package com.funkard.market.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private Double price;
    
    /**
     * 💱 Valuta del prezzo prodotto (codice ISO 4217, es. EUR, USD, GBP)
     */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EUR";
    
    private Double estimatedValue;
    
    @Column(name = "user_id")
    private String userId;
    
    /**
     * 🌍 Descrizione prodotto originale (testo scritto dall'utente)
     * Salvata nella lingua originale per traduzione on-demand
     */
    @Column(name = "description_original", columnDefinition = "TEXT")
    private String descriptionOriginal;
    
    /**
     * 🌍 Lingua originale della descrizione (codice ISO 639-1, es. "it", "en", "es")
     */
    @Column(name = "description_language", length = 5)
    private String descriptionLanguage;
    
    /**
     * 🌍 Nome prodotto tradotto in inglese
     * Solo se l'utente lo inserisce in una lingua diversa da "en"
     */
    @Column(name = "name_en", length = 255)
    private String nameEn;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Constructors, getters, setters handled by Lombok @Data
}
