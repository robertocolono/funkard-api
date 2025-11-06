package com.funkard.adminauth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 📦 Repository per AdminToken
 */
@Repository
public interface AdminTokenRepository extends JpaRepository<AdminToken, UUID> {
    
    /**
     * Trova un token per valore
     */
    Optional<AdminToken> findByToken(String token);
    
    /**
     * Trova tutti i token attivi
     */
    List<AdminToken> findByActiveTrue();
    
    /**
     * Trova tutti i token per ruolo
     */
    List<AdminToken> findByRole(String role);
    
    /**
     * Trova tutti i token attivi per ruolo
     */
    List<AdminToken> findByRoleAndActiveTrue(String role);
}

