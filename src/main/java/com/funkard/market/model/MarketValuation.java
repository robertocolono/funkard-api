package com.funkard.market.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_valuation")
public class MarketValuation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;      // Nome generico (carta, box, ecc.)
    private String setName;
    private String category;      // CARD, BOX, ETB, BOOSTER, SLAB, ACCESSORY
    private String condition;     // MINT, NM, SEALED, ecc.

    private Double avgPrice;
    private Double lastSoldPrice;
    private Boolean estimatedValueProvvisorio = false;
    private Boolean manualCheck = false;

    private LocalDateTime updatedAt;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getSetName() { return setName; }
    public void setSetName(String setName) { this.setName = setName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public Double getAvgPrice() { return avgPrice; }
    public void setAvgPrice(Double avgPrice) { this.avgPrice = avgPrice; }

    public Double getLastSoldPrice() { return lastSoldPrice; }
    public void setLastSoldPrice(Double lastSoldPrice) { this.lastSoldPrice = lastSoldPrice; }

    public Boolean getEstimatedValueProvvisorio() { return estimatedValueProvvisorio; }
    public void setEstimatedValueProvvisorio(Boolean estimatedValueProvvisorio) { this.estimatedValueProvvisorio = estimatedValueProvvisorio; }

    public Boolean getManualCheck() { return manualCheck; }
    public void setManualCheck(Boolean manualCheck) { this.manualCheck = manualCheck; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
