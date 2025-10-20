package com.funkard.admin.dto;

import com.funkard.admin.model.SupportTicket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TicketDTO {
    public UUID id;
    public String userId;
    public String userEmail;
    public String subject;
    public String message;
    public String category;
    public String priority;
    public String status;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public List<SupportMessageDTO> messages;

    // Constructors
    public TicketDTO() {}

    public TicketDTO(UUID id, String userId, String userEmail, String subject, String message, String category, String priority, String status, LocalDateTime createdAt, LocalDateTime updatedAt, List<SupportMessageDTO> messages) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.subject = subject;
        this.message = message;
        this.category = category;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messages = messages;
    }

    // Static factory method
    public static TicketDTO fromEntity(SupportTicket ticket) {
        return new TicketDTO(
            ticket.getId(),
            ticket.getUserId(),
            ticket.getUserEmail(),
            ticket.getSubject(),
            ticket.getMessage(),
            ticket.getCategory(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt(),
            ticket.getMessages().stream()
                    .map(SupportMessageDTO::new)
                    .collect(Collectors.toList())
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<SupportMessageDTO> getMessages() { return messages; }
    public void setMessages(List<SupportMessageDTO> messages) { this.messages = messages; }
}
