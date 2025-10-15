package com.funkard.admin.dto;

import java.time.LocalDate;

public class MarketOverviewDTO {
    public LocalDate day;
    public long newProducts;
    public long pendingItems;

    public MarketOverviewDTO() {}

    public MarketOverviewDTO(LocalDate day, long newProducts, long pendingItems) {
        this.day = day;
        this.newProducts = newProducts;
        this.pendingItems = pendingItems;
    }

    // Getters and Setters
    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }

    public long getNewProducts() { return newProducts; }
    public void setNewProducts(long newProducts) { this.newProducts = newProducts; }

    public long getPendingItems() { return pendingItems; }
    public void setPendingItems(long pendingItems) { this.pendingItems = pendingItems; }
}
