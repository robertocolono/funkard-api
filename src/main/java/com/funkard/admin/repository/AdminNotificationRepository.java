package com.funkard.admin.repository;

import com.funkard.admin.model.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, UUID> {

    List<AdminNotification> findByArchivedFalseOrderByCreatedAtAsc();

    @Query("""
        SELECT n FROM AdminNotification n
        WHERE (:type IS NULL OR n.type = :type)
          AND (:priority IS NULL OR n.priority = :priority)
          AND (:status IS NULL
              OR (:status = 'attiva' AND n.archived = false)
              OR (:status = 'archiviata' AND n.archived = true)
              OR (:status = 'risolta' AND n.resolvedAt IS NOT NULL))
        ORDER BY n.createdAt ASC
    """)
    List<AdminNotification> filter(String type, String priority, String status);

    long deleteByArchivedTrueAndArchivedAtBefore(LocalDateTime olderThan);
    
    // Metodi aggiuntivi per compatibilità
    long countByReadFalse();
    long countByPriority(String priority);
    long countByType(String type);
    List<AdminNotification> findByReadTrueAndCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);
    long deleteAllResolvedBefore(LocalDateTime cutoff);
}