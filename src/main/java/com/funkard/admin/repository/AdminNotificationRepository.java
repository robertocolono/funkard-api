package com.funkard.admin.repository;

import com.funkard.admin.model.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, UUID> {
    
    List<AdminNotification> findByReadFalseOrderByCreatedAtDesc();
    
    List<AdminNotification> findAllByOrderByCreatedAtDesc();
    
    List<AdminNotification> findByTypeOrderByCreatedAtDesc(String type);
    
    List<AdminNotification> findByPriorityOrderByCreatedAtDesc(String priority);
    
    long countByReadFalse();
    
    long countByType(String type);
    
    long countByPriority(String priority);
    
    @Query("SELECT n FROM AdminNotification n WHERE n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<AdminNotification> findRecentNotifications(LocalDateTime since);
    
    // 📊 Dashboard statistics methods
    long countByResolvedFalse();
    
    long countByResolvedTrue();
    
    long countBySeverity(String severity);
    
    long countByTypeAndResolvedFalse(String type);
    
    long countByTypeAndResolvedTrue(String type);
    
    long deleteByResolvedTrueAndCreatedAtBefore(LocalDate date);
}