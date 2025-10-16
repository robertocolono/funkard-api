package com.funkard.admin.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {
    
    List<AdminNotification> findByReadFalseOrderByCreatedAtDesc();
    
    List<AdminNotification> findByResolvedFalseOrderByCreatedAtDesc();
    
    List<AdminNotification> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
    
    List<AdminNotification> findByType(String type);
    
    List<AdminNotification> findByTypeAndResolvedFalse(String type);
    
    @Query("SELECT a FROM AdminNotification a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AdminNotification> findRecentNotifications(LocalDateTime since);
    
    @Query("SELECT a FROM AdminNotification a WHERE a.resolved = false AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AdminNotification> findRecentUnresolved(LocalDateTime since);
    
    long countByReadFalse();
    
    long countByResolvedFalse();
    
    long countByResolvedTrue();
    
    long countByType(String type);
    
    long countByTypeAndResolvedFalse(String type);

    @Modifying
    @Query("DELETE FROM AdminNotification n WHERE n.resolved = true AND n.resolvedAt < :cutoff")
    int deleteAllResolvedBefore(LocalDateTime cutoff);
}
