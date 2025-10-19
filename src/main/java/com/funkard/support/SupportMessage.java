package com.funkard.support;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "support_messages", indexes = {
    @Index(name = "idx_ticket_id", columnList = "ticket_id")
})
public class SupportMessage {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false, length = 100)
    private String sender; // email utente o "admin"

    private OffsetDateTime createdAt = OffsetDateTime.now();

    // === Costruttori ===
    public SupportMessage() {}

    public SupportMessage(SupportTicket ticket, String message, String sender) {
        this.ticket = ticket;
        this.message = message;
        this.sender = sender;
    }

    // === Getters & Setters ===
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public SupportTicket getTicket() { return ticket; }
    public void setTicket(SupportTicket ticket) { this.ticket = ticket; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
