package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 📚 Modello per catalogo franchise
 * 
 * Gestisce i franchise disponibili per le carte, con possibilità
 * di attivazione/disattivazione dal pannello admin.
 */
@Entity
@Table(name = "franchise_catalog", indexes = {
    @Index(name = "idx_franchise_category", columnList = "category"),
    @Index(name = "idx_franchise_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FranchiseCatalog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Categoria del franchise (es. "TCG", "Anime", "TCG / Anime")
     */
    @Column(nullable = false, length = 100)
    private String category;
    
    /**
     * Nome del franchise (es. "Pokémon", "Yu-Gi-Oh!", "MetaZoo")
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Flag attivazione (default: true)
     * Se false, il franchise non appare nelle liste disponibili
     */
    @Column(nullable = false)
    private Boolean active = true;
    
    /**
     * Data creazione
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Data ultimo aggiornamento
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}

