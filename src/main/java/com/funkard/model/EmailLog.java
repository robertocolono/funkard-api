package com.funkard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 📧 Modello per log email inviate
 * 
 * Traccia tutte le email inviate per audit, debugging e trasparenza.
 */
@Entity
@Table(name = "email_logs", indexes = {
    @Index(name = "idx_email_logs_recipient", columnList = "recipient"),
    @Index(name = "idx_email_logs_type", columnList = "type"),
    @Index(name = "idx_email_logs_status", columnList = "status"),
    @Index(name = "idx_email_logs_sent_at", columnList = "sent_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * 📧 Email destinatario
     */
    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;
    
    /**
     * 📤 Mittente usato (es. no-reply@funkard.com)
     */
    @Column(name = "sender", nullable = false, length = 255)
    private String sender;
    
    /**
     * 📝 Oggetto email
     */
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;
    
    /**
     * 🏷️ Tipo email (es. ACCOUNT_CONFIRMATION, ORDER_SHIPPED)
     */
    @Column(name = "type", nullable = false, length = 100)
    private String type;
    
    /**
     * 📊 Stato invio
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmailStatus status = EmailStatus.SENT;
    
    /**
     * ❌ Dettaglio errore se fallito
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 📅 Data/ora invio
     */
    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;
    
    /**
     * 🔄 Numero tentativi effettuati
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    /**
     * 🌍 Lingua usata (es. it, en)
     */
    @Column(name = "locale", nullable = false, length = 10)
    private String locale = "it";
    
    /**
     * 📄 Nome template usato
     */
    @Column(name = "template_name", length = 255)
    private String templateName;
    
    /**
     * 🔗 ID webhook se provider esterno (futuro)
     */
    @Column(name = "webhook_id", length = 255)
    private String webhookId;
    
    /**
     * 📊 Enum per stato email
     */
    public enum EmailStatus {
        SENT,      // Inviata con successo
        FAILED,    // Fallita
        RETRIED    // Ritentata dopo errore
    }
}

