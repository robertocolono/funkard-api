package com.funkard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🔐 DTO per response login
 * 
 * Include token JWT e preferenze utente (language, preferredCurrency)
 * per permettere al frontend di sincronizzarsi immediatamente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String language;
    private String preferredCurrency;
}

