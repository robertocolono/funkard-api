package com.funkard.user.payment.dto;

import lombok.*;
import jakarta.validation.constraints.*;

/**
 * 📝 Request DTO per creazione/aggiornamento metodi di pagamento
 * 
 * ✅ Validazione completa
 * 🔒 Sicurezza integrata
 */
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class PaymentMethodRequest {
    
    @NotBlank(message = "Nome del titolare è obbligatorio")
    @Size(max = 100, message = "Nome del titolare troppo lungo")
    private String cardHolder;
    
    @NotBlank(message = "Numero di carta è obbligatorio")
    @Pattern(regexp = "^\\d{4}\\s\\d{4}\\s\\d{4}\\s\\d{4}$", 
             message = "Formato numero carta non valido (usare spazi)")
    private String cardNumber;
    
    @NotBlank(message = "Data di scadenza è obbligatoria")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", 
             message = "Formato data scadenza non valido (MM/YY)")
    private String expiryDate;
    
    @NotBlank(message = "Brand della carta è obbligatorio")
    @Pattern(regexp = "^(VISA|MASTERCARD|AMEX|DISCOVER)$", 
             message = "Brand non supportato")
    private String brand;
    
    @NotBlank(message = "CVV è obbligatorio")
    @Pattern(regexp = "^\\d{3,4}$", 
             message = "CVV non valido")
    private String cvv;
    
    private boolean setAsDefault = false;
    
    /**
     * 🔍 Valida il numero di carta secondo algoritmo Luhn
     */
    public boolean isValidCardNumber() {
        if (cardNumber == null) return false;
        
        String cleanNumber = cardNumber.replaceAll("\\s", "");
        if (cleanNumber.length() < 13 || cleanNumber.length() > 19) return false;
        
        // Algoritmo Luhn
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cleanNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cleanNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }
    
    /**
     * 🎯 Valida la data di scadenza
     */
    public boolean isValidExpiryDate() {
        if (expiryDate == null || !expiryDate.matches("^(0[1-9]|1[0-2])/([0-9]{2})$")) {
            return false;
        }
        
        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000;
            
            LocalDateTime expiry = LocalDateTime.of(year, month, 1, 0, 0);
            return LocalDateTime.now().isBefore(expiry);
        } catch (Exception e) {
            return false;
        }
    }
}
