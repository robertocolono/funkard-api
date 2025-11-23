package com.funkard.dto;

import lombok.Data;

/**
 * ⚙️ DTO per aggiornamento preferenze utente (language e preferredCurrency)
 * 
 * Usato per endpoint PATCH /api/user/preferences
 */
@Data
public class UserPreferencesDTO {
    private String language;
    private String preferredCurrency;
}

