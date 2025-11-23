package com.funkard.adminauth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 📦 Repository per AdminUser
 */
@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {
    
    /**
     * Trova un utente admin per token di accesso
     */
    Optional<AdminUser> findByAccessToken(String accessToken);
    
    /**
     * Trova un utente admin per email
     */
    Optional<AdminUser> findByEmail(String email);
    
    /**
     * Verifica se esiste un SUPER_ADMIN attivo
     */
    boolean existsByRoleAndActiveTrue(String role);
    
    /**
     * Trova il primo SUPER_ADMIN attivo
     */
    Optional<AdminUser> findFirstByRoleAndActiveTrue(String role);
    
    /**
     * Trova tutti gli utenti con pending=true
     */
    List<AdminUser> findByPendingTrue();
}

