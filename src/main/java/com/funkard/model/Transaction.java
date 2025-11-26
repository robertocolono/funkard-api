package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @ManyToOne
    @JoinColumn(name = "listing_id")
    private Listing listing;

    private Double price;
    
    /**
     * 💱 Valuta della transazione (codice ISO 4217, es. EUR, USD, GBP)
     */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EUR";
    
    private String status = "PENDING";
    private LocalDateTime createdAt = LocalDateTime.now();
}
