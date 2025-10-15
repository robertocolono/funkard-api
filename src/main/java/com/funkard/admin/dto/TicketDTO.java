package com.funkard.admin.dto;

import com.funkard.admin.model.SupportTicket;
import java.time.LocalDateTime;
import java.util.UUID;

public class TicketDTO {
    public UUID id;
    public String userId;
    public String subject;
    public String message;
    public String status;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    // Constructors
    public TicketDTO() {}

    public TicketDTO(UUID id, String userId, String subject, String message, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Static factory method
    public static TicketDTO fromEntity(SupportTicket ticket) {
        return new TicketDTO(
            ticket.getId(),
            ticket.getUserId(),
            ticket.getSubject(),
            ticket.getMessage(),
            ticket.getStatus(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt()
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
