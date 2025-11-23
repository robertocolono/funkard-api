package com.funkard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 🗑️ Modello per tracciamento richieste cancellazione account (GDPR Art. 17)
 * 
 * Gestisce il periodo di grazia di 7 giorni prima della cancellazione definitiva.
 */
@Entity
@Table(name = "user_deletions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 🔗 ID utente da cancellare
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    /**
     * 📧 Email utente (salvata per log anche dopo cancellazione)
     */
    @Column(name = "email", nullable = false)
    private String email;
    
    /**
     * 📅 Data richiesta cancellazione
     */
    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;
    
    /**
     * 📅 Data programmata per cancellazione definitiva (requestedAt + 7 giorni)
     */
    @Column(name = "scheduled_deletion_at", nullable = false)
    private LocalDateTime scheduledDeletionAt;
    
    /**
     * 📊 Stato cancellazione
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeletionStatus status = DeletionStatus.PENDING;
    
    /**
     * 📝 Motivo cancellazione (opzionale)
     */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    /**
     * 📅 Data completamento cancellazione (se completata)
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * 📊 Enum per stato cancellazione
     */
    public enum DeletionStatus {
        PENDING,    // In attesa di cancellazione (periodo grazia)
        COMPLETED,  // Cancellazione completata
        FAILED      // Errore durante cancellazione (da ritentare)
    }
}

