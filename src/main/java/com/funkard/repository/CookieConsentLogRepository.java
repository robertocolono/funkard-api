package com.funkard.repository;

import com.funkard.model.CookieConsentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 📦 Repository per audit log consenso cookie
 */
@Repository
public interface CookieConsentLogRepository extends JpaRepository<CookieConsentLog, Long> {
    
    /**
     * 🔍 Trova tutti i log per un utente (ordinati per data)
     * @param userId ID utente
     * @return Lista log ordinati per data (più recenti prima)
     */
    List<CookieConsentLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 🔍 Trova ultimo log per un utente
     * @param userId ID utente
     * @return Ultimo log o null
     */
    @Query("SELECT l FROM CookieConsentLog l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    List<CookieConsentLog> findLastByUserId(@Param("userId") Long userId);
    
    /**
     * 🔍 Trova primo log (più recente) per un utente
     * @param userId ID utente
     * @return Optional del primo log o empty
     */
    default java.util.Optional<CookieConsentLog> findMostRecentByUserId(Long userId) {
        List<CookieConsentLog> logs = findLastByUserId(userId);
        return logs.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(logs.get(0));
    }
    
    /**
     * 🗑️ Elimina tutti i log per un utente (GDPR: diritto alla cancellazione)
     * @param userId ID utente
     */
    void deleteByUserId(Long userId);
}

