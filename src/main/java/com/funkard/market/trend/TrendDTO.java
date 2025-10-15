package com.funkard.market.trend;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TrendDTO {
    public String itemName;
    public String category;
    public String rangeType; // 7d | 30d | 1y
    public List<Map<String,Object>> points; // [{date, avgPrice}]
    public Double lastSoldPrice;            // opzionale
    public LocalDateTime updatedAt;
    public String status;                   // ok | insufficient_data | new_item
    public boolean manualCheck;             // suggerimento per pannello admin

    // Constructors
    public TrendDTO() {}

    public TrendDTO(String itemName, String category, String rangeType) {
        this.itemName = itemName;
        this.category = category;
        this.rangeType = rangeType;
    }

    // Getters and Setters
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRangeType() { return rangeType; }
    public void setRangeType(String rangeType) { this.rangeType = rangeType; }

    public List<Map<String,Object>> getPoints() { return points; }
    public void setPoints(List<Map<String,Object>> points) { this.points = points; }

    public Double getLastSoldPrice() { return lastSoldPrice; }
    public void setLastSoldPrice(Double lastSoldPrice) { this.lastSoldPrice = lastSoldPrice; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isManualCheck() { return manualCheck; }
    public void setManualCheck(boolean manualCheck) { this.manualCheck = manualCheck; }
}
