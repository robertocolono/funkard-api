package com.funkard.adminauth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 📦 Repository per AccessRequest
 */
@Repository
public interface AccessRequestRepository extends JpaRepository<AccessRequest, UUID> {
    
    /**
     * Trova tutte le richieste con status PENDING
     */
    List<AccessRequest> findByStatus(String status);
    
    /**
     * Trova una richiesta per email
     */
    Optional<AccessRequest> findByEmail(String email);
    
    /**
     * Trova una richiesta per token usato
     */
    Optional<AccessRequest> findByTokenUsed(String tokenUsed);
    
    /**
     * Trova tutte le richieste pending
     */
    List<AccessRequest> findByStatusOrderByCreatedAtDesc(String status);
}

