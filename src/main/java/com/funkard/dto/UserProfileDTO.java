package com.funkard.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 👤 DTO per profilo utente
 * 
 * ✅ Dati essenziali per il frontend
 * ✅ Compatibile con User entity
 */
@Data
public class UserProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String username;
    private String preferredCurrency;
    private String language;
    private String theme;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
