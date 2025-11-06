package com.funkard.adminaccess.repository;

import com.funkard.adminaccess.model.AdminAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    List<AdminAccessToken> findByActiveTrue();
    
    /**
     * Trova tutti i token per ruolo
     */
    List<AdminAccessToken> findByRole(String role);
}

