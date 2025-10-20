package com.funkard.admin.dto;

import com.funkard.admin.model.SupportMessage;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SupportMessageDTO {
    private UUID id;
    private String sender;
    private String content;
    private OffsetDateTime createdAt;

    // Constructors
    public SupportMessageDTO() {}

    public SupportMessageDTO(UUID id, String sender, String content, OffsetDateTime createdAt) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.createdAt = createdAt;
    }

    public SupportMessageDTO(SupportMessage msg) {
        this.id = msg.getId();
        this.sender = msg.getSender();
        this.content = msg.getMessage();
        this.createdAt = msg.getCreatedAt();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
