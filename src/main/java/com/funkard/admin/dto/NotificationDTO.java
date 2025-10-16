package com.funkard.admin.dto;

import com.funkard.admin.model.AdminNotification;
import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDTO {
    public UUID id;
    public String type;
    public String title;
    public String message;
    public String priority;
    public boolean read;
    public LocalDateTime createdAt;
    public LocalDateTime readAt;
    
    public static NotificationDTO fromEntity(AdminNotification entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.id = entity.getId();
        dto.type = entity.getType();
        dto.title = entity.getTitle();
        dto.message = entity.getMessage();
        dto.priority = entity.getPriority();
        dto.read = entity.isRead();
        dto.createdAt = entity.getCreatedAt();
        dto.readAt = entity.getReadAt();
        return dto;
    }
}
