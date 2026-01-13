package com.funkard.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private BigDecimal price;
    
    /**
     * 💱 Valuta del prezzo listing (codice ISO 4217, es. EUR, USD, GBP)
     */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EUR";
    
    private String condition;
    private String seller;
    
    /**
     * 🔄 Indica se questa vendita accetta scambi
     */
    @Column(name = "accept_trades", nullable = false)
    private boolean acceptTrades = false;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getSeller() { return seller; }
    public void setSeller(String seller) { this.seller = seller; }

    public Card getCard() { return card; }
    public void setCard(Card card) { this.card = card; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public boolean isAcceptTrades() { return acceptTrades; }
    public void setAcceptTrades(boolean acceptTrades) { this.acceptTrades = acceptTrades; }
}
