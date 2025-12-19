package com.funkard.adminauthmodern;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * 📦 Repository per AdminSession
 */
@Repository
public interface AdminSessionRepository extends JpaRepository<AdminSession, UUID> {
    
    /**
     * Trova una sessione per sessionId
     */
    Optional<AdminSession> findBySessionId(String sessionId);
    
    /**
     * Elimina tutte le sessioni scadute
     * Usa Instant (UTC) per confronto timezone-safe
     */
    @Modifying
    @Query("DELETE FROM AdminSession s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(@Param("now") Instant now);
    
    /**
     * Elimina tutte le sessioni di un admin
     */
    @Modifying
    @Query("DELETE FROM AdminSession s WHERE s.adminId = :adminId")
    int deleteByAdminId(@Param("adminId") UUID adminId);
    
    /**
     * Elimina una sessione specifica
     */
    @Modifying
    @Query("DELETE FROM AdminSession s WHERE s.sessionId = :sessionId")
    int deleteBySessionId(@Param("sessionId") String sessionId);
}

