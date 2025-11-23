package com.funkard.repository;

import com.funkard.model.EmailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 📦 Repository per gestione log email
 */
@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {
    
    /**
     * 🔍 Trova log per destinatario
     */
    Page<EmailLog> findByRecipientOrderBySentAtDesc(String recipient, Pageable pageable);
    
    /**
     * 🔍 Trova log per tipo
     */
    Page<EmailLog> findByTypeOrderBySentAtDesc(String type, Pageable pageable);
    
    /**
     * 🔍 Trova log per stato
     */
    Page<EmailLog> findByStatusOrderBySentAtDesc(EmailLog.EmailStatus status, Pageable pageable);
    
    /**
     * 🔍 Trova log per destinatario e tipo
     */
    Page<EmailLog> findByRecipientAndTypeOrderBySentAtDesc(
        String recipient, 
        String type, 
        Pageable pageable
    );
    
    /**
     * 🔍 Trova log per destinatario, tipo e stato
     */
    Page<EmailLog> findByRecipientAndTypeAndStatusOrderBySentAtDesc(
        String recipient,
        String type,
        EmailLog.EmailStatus status,
        Pageable pageable
    );
    
    /**
     * 🔍 Trova log per range date
     */
    @Query("SELECT e FROM EmailLog e WHERE e.sentAt BETWEEN :fromDate AND :toDate ORDER BY e.sentAt DESC")
    Page<EmailLog> findBySentAtBetween(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable
    );
    
    /**
     * 🔍 Trova log vecchi (per cleanup)
     */
    @Query("SELECT e FROM EmailLog e WHERE e.sentAt < :cutoffDate")
    List<EmailLog> findOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 🔍 Query complessa con filtri multipli
     */
    @Query("SELECT e FROM EmailLog e WHERE " +
           "(:recipient IS NULL OR e.recipient = :recipient) AND " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:fromDate IS NULL OR e.sentAt >= :fromDate) AND " +
           "(:toDate IS NULL OR e.sentAt <= :toDate) " +
           "ORDER BY e.sentAt DESC")
    Page<EmailLog> findWithFilters(
        @Param("recipient") String recipient,
        @Param("type") String type,
        @Param("status") EmailLog.EmailStatus status,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable
    );
}

