package com.funkard.repository;

import com.funkard.model.UserDeletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 📦 Repository per gestione richieste cancellazione account
 */
@Repository
public interface UserDeletionRepository extends JpaRepository<UserDeletion, Long> {
    
    /**
     * 🔍 Trova richiesta per ID utente
     * @param userId ID utente
     * @return Optional<UserDeletion>
     */
    Optional<UserDeletion> findByUserId(Long userId);
    
    /**
     * 🔍 Trova tutte le richieste pending pronte per cancellazione
     * @param status Stato
     * @param scheduledDeletionAt Data programmata
     * @return Lista richieste
     */
    List<UserDeletion> findByStatusAndScheduledDeletionAtLessThanEqual(
        UserDeletion.DeletionStatus status, 
        LocalDateTime scheduledDeletionAt
    );
    
    /**
     * 🔍 Trova tutte le richieste per stato
     * @param status Stato
     * @return Lista richieste
     */
    List<UserDeletion> findByStatus(UserDeletion.DeletionStatus status);
}

