package com.funkard.adminauth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 📦 Repository per AdminAccessToken
 */
@Repository
public interface AdminAccessTokenRepository extends JpaRepository<AdminAccessToken, UUID> {
    
    /**
     * Trova un token per valore
     */
    Optional<AdminAccessToken> findByToken(String token);
    
    /**
     * Trova tutti i token attivi
     */
    java.util.List<AdminAccessToken> findByActiveTrue();
    
    /**
     * Trova tutti i token per ruolo
     */
    java.util.List<AdminAccessToken> findByRole(String role);
}

