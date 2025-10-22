package com.funkard.user.payment.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 💳 DTO per metodi di pagamento
 * 
 * 🔒 SICUREZZA: Solo dati pubblici, mai numeri completi
 * ✅ Compatibile con frontend React
 * ✅ Validazione integrata
 */
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class PaymentMethodDTO {
    
    private String id;
    private String cardHolder;
    private String cardNumberMasked;
    private String expiryDate;
    private String brand;
    private boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Campi aggiuntivi per UI
    private String displayName;
    private boolean isExpired;
    private String lastFourDigits;
    
    /**
     * 🏷️ Nome visualizzato per UI
     */
    public String getDisplayName() {
        if (displayName != null) return displayName;
        return String.format("%s •••• %s", brand, getLastFourDigits());
    }
    
    /**
     * 🔢 Ultime 4 cifre della carta
     */
    public String getLastFourDigits() {
        if (lastFourDigits != null) return lastFourDigits;
        if (cardNumberMasked == null || cardNumberMasked.length() < 4) return "****";
        return cardNumberMasked.substring(cardNumberMasked.length() - 4);
    }
    
    /**
     * ⏰ Verifica se la carta è scaduta
     */
    public boolean isExpired() {
        if (isExpired) return true;
        if (expiryDate == null || expiryDate.length() < 5) return false;
        
        try {
            String[] parts = expiryDate.split("/");
            if (parts.length != 2) return false;
            
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000;
            
            LocalDateTime expiry = LocalDateTime.of(year, month, 1, 0, 0);
            return LocalDateTime.now().isAfter(expiry);
        } catch (Exception e) {
            return false;
        }
    }
}
