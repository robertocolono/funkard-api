package com.funkard.user.payment;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 💳 Entità per gestione metodi di pagamento utenti
 * 
 * 🔒 SICUREZZA: Mai salviamo numeri di carta completi
 * ✅ Solo versioni mascherate (**** **** **** 1234)
 * ✅ Integrazione futura con Stripe
 */
@Entity
@Table(name = "user_payment_methods")
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "card_holder", nullable = false)
    private String cardHolder;

    @Column(name = "card_number_masked", nullable = false)
    private String cardNumberMasked;

    @Column(name = "expiry_date", nullable = false)
    private String expiryDate;

    @Column(name = "brand", nullable = false)
    private String brand; // VISA, MASTERCARD, AMEX, etc.

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Metodi di utilità
    public boolean isExpired() {
        if (expiryDate == null || expiryDate.length() < 5) return false;
        
        try {
            String[] parts = expiryDate.split("/");
            if (parts.length != 2) return false;
            
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000; // Assume anni 20xx
            
            LocalDateTime expiry = LocalDateTime.of(year, month, 1, 0, 0);
            return LocalDateTime.now().isAfter(expiry);
        } catch (Exception e) {
            return false;
        }
    }

    public String getDisplayName() {
        return String.format("%s •••• %s", brand, 
            cardNumberMasked.substring(cardNumberMasked.length() - 4));
    }
}
