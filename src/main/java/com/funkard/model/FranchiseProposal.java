package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 📝 Modello per proposte franchise da parte degli utenti
 * 
 * Gestisce le richieste di nuovi franchise che richiedono
 * approvazione admin prima di essere aggiunti al catalogo.
 */
@Entity
@Table(name = "franchise_proposals", indexes = {
    @Index(name = "idx_proposal_category", columnList = "category"),
    @Index(name = "idx_proposal_status", columnList = "status"),
    @Index(name = "idx_proposal_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FranchiseProposal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Categoria proposta (es. "TCG", "Anime", "Sportive")
     */
    @Column(nullable = false, length = 100)
    private String category;
    
    /**
     * Nome franchise proposto (es. "NBA Prizm", "UFC Prizm")
     */
    @Column(nullable = false, length = 100)
    private String franchise;
    
    /**
     * Email utente che ha proposto (opzionale - può essere anonimo)
     */
    @Column(name = "user_email", length = 255)
    private String userEmail;
    
    /**
     * ID utente che ha proposto (opzionale)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    /**
     * Stato della proposta
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProposalStatus status = ProposalStatus.PENDING;
    
    /**
     * Admin che ha processato la proposta (opzionale)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;
    
    /**
     * Data processamento (approvazione/rifiuto)
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Enum per stato proposta
     */
    public enum ProposalStatus {
        PENDING,   // In attesa di approvazione
        APPROVED,  // Approvata e aggiunta al catalogo
        REJECTED   // Rifiutata
    }
}

