package com.funkard.admin.repository;

import com.funkard.admin.model.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // 🔍 Filtro dinamico per tipo, gravità, stato e limite
    @Query("""
        SELECT n FROM AdminNotification n
        WHERE (:type IS NULL OR n.type = :type)
        AND (:priority IS NULL OR n.priority = :priority)
        AND (:read IS NULL OR n.read = :read)
        ORDER BY n.createdAt DESC
    """)
    List<AdminNotification> filter(
            @Param("type") String type,
            @Param("priority") String priority,
            @Param("read") Boolean read
    );
    
    @Query("DELETE FROM AdminNotification n WHERE n.read = true AND n.createdAt < :cutoff")
    int deleteAllResolvedBefore(@Param("cutoff") LocalDateTime cutoff);
}