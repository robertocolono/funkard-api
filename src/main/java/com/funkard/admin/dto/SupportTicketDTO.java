package com.funkard.admin.dto;

import com.funkard.admin.model.SupportTicket;
import java.time.LocalDateTime;
import java.util.UUID;

public class SupportTicketDTO {
    public UUID id;
    public String userId;
    public String userEmail;
    public String subject;
    public String message;
    public String status;
    public String priority;
    public String category;
    public String adminResponse;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime resolvedAt;
    
    public static SupportTicketDTO fromEntity(SupportTicket entity) {
        SupportTicketDTO dto = new SupportTicketDTO();
        dto.id = entity.getId();
        dto.userId = entity.getUserId();
        dto.userEmail = entity.getUserEmail();
        dto.subject = entity.getSubject();
        dto.message = entity.getMessage();
        dto.status = entity.getStatus();
        dto.priority = entity.getPriority();
        dto.category = entity.getCategory();
        dto.adminResponse = entity.getAdminResponse();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        dto.resolvedAt = entity.getResolvedAt();
        return dto;
    }
}
