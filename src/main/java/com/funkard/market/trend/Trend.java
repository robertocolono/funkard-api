package com.funkard.market.trend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trends")
public class Trend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "item_name")
    private String itemName;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "range_type")
    private String rangeType;
    
    @Column(name = "data_points", columnDefinition = "TEXT")
    private String dataPoints;
    
    @Column(name = "manual_check")
    private boolean manualCheck = false;
    
    @Column(name = "source")
    private String source;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public Trend() {}

    public Trend(String itemName, String category, String rangeType, String dataPoints, boolean manualCheck, String source) {
        this.itemName = itemName;
        this.category = category;
        this.rangeType = rangeType;
        this.dataPoints = dataPoints;
        this.manualCheck = manualCheck;
        this.source = source;
        this.updatedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRangeType() { return rangeType; }
    public void setRangeType(String rangeType) { this.rangeType = rangeType; }

    public String getDataPoints() { return dataPoints; }
    public void setDataPoints(String dataPoints) { this.dataPoints = dataPoints; }

    public boolean isManualCheck() { return manualCheck; }
    public void setManualCheck(boolean manualCheck) { this.manualCheck = manualCheck; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
