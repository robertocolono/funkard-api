package com.funkard.admin.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminNotificationArchiveRepository extends JpaRepository<AdminNotificationArchive, Long> {
    
    List<AdminNotificationArchive> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
    
    List<AdminNotificationArchive> findByType(String type);
    
    List<AdminNotificationArchive> findByArchivedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT a FROM AdminNotificationArchive a WHERE a.archivedAt >= :since ORDER BY a.archivedAt DESC")
    List<AdminNotificationArchive> findRecentArchived(LocalDateTime since);
    
    @Modifying
    @Query("DELETE FROM AdminNotificationArchive a WHERE a.resolvedAt < :cutoff")
    void deleteByResolvedAtBefore(LocalDateTime cutoff);
    
    long countByType(String type);
    
    long countByArchivedAtBetween(LocalDateTime start, LocalDateTime end);
}
