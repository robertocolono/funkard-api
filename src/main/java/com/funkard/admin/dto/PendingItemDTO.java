package com.funkard.admin.dto;

import java.time.LocalDateTime;

public class PendingItemDTO {
    public String itemName;
    public String category;
    public String rangeType;
    public String source;
    public LocalDateTime updatedAt;

    // Constructors
    public PendingItemDTO() {}

    public PendingItemDTO(String itemName, String category, String rangeType, String source, LocalDateTime updatedAt) {
        this.itemName = itemName;
        this.category = category;
        this.rangeType = rangeType;
        this.source = source;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRangeType() { return rangeType; }
    public void setRangeType(String rangeType) { this.rangeType = rangeType; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
