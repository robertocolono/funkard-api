package com.funkard.adminauth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 📦 Repository per AdminAccessRequest
 */
@Repository
public interface AdminAccessRequestRepository extends JpaRepository<AdminAccessRequest, UUID> {
    
    /**
     * Trova tutte le richieste per status
     */
    List<AdminAccessRequest> findByStatus(String status);
    
    /**
     * Trova una richiesta per email
     */
    Optional<AdminAccessRequest> findByEmail(String email);
    
    /**
     * Trova tutte le richieste pending
     */
    List<AdminAccessRequest> findByStatusOrderByCreatedAtDesc(String status);
}

