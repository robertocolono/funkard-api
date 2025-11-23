package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ⏳ Modello per valori personalizzati "Altro" proposti dagli utenti
 * 
 * Gestisce proposte di nuovi valori TCG o Lingua che richiedono
 * approvazione admin prima di essere aggiunti alle liste ufficiali.
 */
@Entity
@Table(name = "pending_values", indexes = {
    @Index(name = "idx_pending_type", columnList = "type"),
    @Index(name = "idx_pending_approved", columnList = "approved"),
    @Index(name = "idx_pending_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingValue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * Tipo di valore proposto
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ValueType type;
    
    /**
     * Valore proposto dall'utente
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;
    
    /**
     * Utente che ha proposto il valore
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;
    
    /**
     * Data creazione proposta
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Flag approvazione (default: false)
     */
    @Column(nullable = false)
    private Boolean approved = false;
    
    /**
     * Admin che ha approvato (nullable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    /**
     * Data approvazione (nullable)
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    /**
     * Enum per tipo di valore
     */
    public enum ValueType {
        TCG,       // Trading Card Game
        LANGUAGE,  // Lingua
        FRANCHISE  // Franchise/Marchio
    }
}

