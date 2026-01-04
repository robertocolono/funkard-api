package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String setName;
    private String rarity;
    private Integer grade;
    private String imageUrl;
    private Double marketValue = 0.0;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Campi opzionali per la gestione della collezione personale
    private String condition; // es: MINT, NEAR_MINT, EXCELLENT, GOOD, FAIR, POOR
    private String source;    // es: "collection", "market", ecc.
    
    // 🌍 Campi categoria e franchise
    @Column(length = 100)
    private String category; // es: "TCG", "Anime", "TCG / Anime"
    
    @Column(length = 100)
    private String franchise; // es: "Pokémon", "Yu-Gi-Oh!", "Magic: The Gathering"
    
    @Column(length = 50)
    private String language; // es: "Italiano", "Inglese", "Giapponese"
    
    /**
     * 📦 Tipo della Card (SINGLE_CARD, SEALED_BOX, BOOSTER_PACK, STARTER_DECK, COMPLETE_SET, PROMO, ACCESSORY)
     */
    @Column(length = 50)
    private String type;

    @OneToMany(mappedBy = "card")
    private List<Listing> listings;
}
