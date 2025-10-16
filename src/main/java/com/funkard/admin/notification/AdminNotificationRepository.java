package com.funkard.admin.notification;

import com.funkard.admin.notification.AdminNotification.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
    int deleteAllResolvedBefore(LocalDateTime cutoff);
}
