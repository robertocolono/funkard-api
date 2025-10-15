package com.funkard.admin.dto;

import com.funkard.admin.model.AdminNotification;
import java.time.LocalDateTime;
import java.util.UUID;

public class AdminNotificationDTO {
    public UUID id;
    public String productId;
    public String message;
    public boolean resolved;
    public LocalDateTime createdAt;

    public AdminNotificationDTO() {}

    public AdminNotificationDTO(UUID id, String productId, String message, boolean resolved, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.message = message;
        this.resolved = resolved;
        this.createdAt = createdAt;
    }

    public static AdminNotificationDTO fromEntity(AdminNotification entity) {
        return new AdminNotificationDTO(
            entity.getId(),
            entity.getProductId(),
            entity.getMessage(),
            entity.isResolved(),
            entity.getCreatedAt()
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
