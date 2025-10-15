package com.funkard.market.model;

import java.time.LocalDateTime;

// Semplice modello per vendite recenti; implementazione stub
public class MarketListing {
    private String itemName;
    private String setName;
    private String category;
    private String condition;
    private double priceEUR;
    private LocalDateTime soldAt;

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getSetName() { return setName; }
    public void setSetName(String setName) { this.setName = setName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public double getPriceEUR() { return priceEUR; }
    public void setPriceEUR(double priceEUR) { this.priceEUR = priceEUR; }

    public LocalDateTime getSoldAt() { return soldAt; }
    public void setSoldAt(LocalDateTime soldAt) { this.soldAt = soldAt; }
}
