package com.funkard.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 📝 Request DTO per registrazione utenti
 * 
 * ✅ Include campo preferredCurrency
 * ✅ Compatibile con frontend esistente
 * 🔒 GDPR Compliance: Accettazione obbligatoria Termini e Privacy Policy
 */
@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String preferredCurrency;
    private String language;
    
    // 🔒 GDPR Compliance: Accettazione obbligatoria
    @NotNull(message = "L'accettazione dei Termini e Condizioni è obbligatoria")
    private Boolean acceptTerms;
    
    @NotNull(message = "L'accettazione della Privacy Policy è obbligatoria")
    private Boolean acceptPrivacy;
}
