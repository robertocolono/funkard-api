package com.funkard.admin.notification;

import com.funkard.admin.notification.AdminNotification.Type;
import com.funkard.admin.notification.AdminNotification.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    List<AdminNotification> findByResolvedFalseOrderByCreatedAtDesc();

    List<AdminNotification> findByResolvedTrueOrderByCreatedAtDesc();

    List<AdminNotification> findByTypeAndResolvedFalseOrderByCreatedAtDesc(Type type);

    @Transactional
    @Modifying
    @Query("DELETE FROM AdminNotification n WHERE n.resolved = true AND n.createdAt < :cutoff")
    int deleteAllResolvedBefore(@Param("cutoff") LocalDateTime cutoff);

    // 🔍 Filtro dinamico per tipo, gravità, stato e limite
    @Query("""
        SELECT n FROM AdminNotification n
        WHERE (:type IS NULL OR n.type = :type)
        AND (:severity IS NULL OR n.severity = :severity)
        AND (:resolved IS NULL OR n.resolved = :resolved)
        ORDER BY n.createdAt DESC
    """)
    List<AdminNotification> filter(
            @Param("type") Type type,
            @Param("severity") Severity severity,
            @Param("resolved") Boolean resolved
    );
}
