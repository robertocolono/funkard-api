package com.funkard.payload;

import lombok.Getter;
import lombok.Setter;

/**
 * 📝 Request DTO per registrazione utenti
 * 
 * ✅ Include campo preferredCurrency
 * ✅ Compatibile con frontend esistente
 */
@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String preferredCurrency; // 👈 aggiunto
}
