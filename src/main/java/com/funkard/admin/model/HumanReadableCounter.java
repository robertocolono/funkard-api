package com.funkard.admin.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 📋 Entity per contatori numerazione umana
 * Gestisce contatori separati per (prefix, year)
 * Esempio: SYS-2025-0001, SYS-2026-0001
 */
@Entity
@Table(name = "human_readable_counters", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"prefix", "year"}))
public class HumanReadableCounter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 10)
    private String prefix;
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(nullable = false)
    private Integer currentValue = 0;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Costruttori
    public HumanReadableCounter() {}
    
    public HumanReadableCounter(String prefix, Integer year) {
        this.prefix = prefix;
        this.year = year;
        this.currentValue = 0;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public Integer getCurrentValue() { return currentValue; }
    public void setCurrentValue(Integer currentValue) { this.currentValue = currentValue; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

