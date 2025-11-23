package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 📚 Modello per franchise ufficiali
 * 
 * Rappresenta un franchise approvato e attivo nel sistema.
 * Sincronizzato automaticamente con franchises.json.
 */
@Entity
@Table(name = "franchises", indexes = {
    @Index(name = "idx_franchise_category", columnList = "category"),
    @Index(name = "idx_franchise_status", columnList = "status"),
    @Index(name = "idx_franchise_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Franchise {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Categoria del franchise (es. "TCG", "Anime", "TCG / Anime")
     */
    @Column(nullable = false, length = 100)
    private String category;
    
    /**
     * Nome del franchise (es. "Pokémon", "Yu-Gi-Oh!")
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    /**
     * Stato del franchise
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FranchiseStatus status = FranchiseStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Enum per stato franchise
     */
    public enum FranchiseStatus {
        ACTIVE,    // Franchise attivo e visibile
        DISABLED   // Franchise disattivato (non visibile pubblicamente)
    }
}

