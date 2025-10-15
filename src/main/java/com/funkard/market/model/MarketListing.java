package com.funkard.market.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_listings")
public class MarketListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "item_name")
    private String itemName;
    
    @Column(name = "set_name")
    private String setName;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "condition")
    private String condition;
    
    @Column(name = "price_eur")
    private double priceEUR;
    
    @Column(name = "sold_at")
    private LocalDateTime soldAt;
    
    @Column(name = "sold")
    private boolean sold = false;

    // Constructors
    public MarketListing() {}

    public MarketListing(String itemName, String setName, String category, String condition, double priceEUR, LocalDateTime soldAt, boolean sold) {
        this.itemName = itemName;
        this.setName = setName;
        this.category = category;
        this.condition = condition;
        this.priceEUR = priceEUR;
        this.soldAt = soldAt;
        this.sold = sold;
    }

    // Getters and Setters
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

    public double getPriceEUR() { return priceEUR; }
    public void setPriceEUR(double priceEUR) { this.priceEUR = priceEUR; }

    public LocalDateTime getSoldAt() { return soldAt; }
    public void setSoldAt(LocalDateTime soldAt) { this.soldAt = soldAt; }

    public boolean isSold() { return sold; }
    public void setSold(boolean sold) { this.sold = sold; }
}
