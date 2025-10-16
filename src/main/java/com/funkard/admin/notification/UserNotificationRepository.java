package com.funkard.admin.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    
    List<UserNotification> findByUserId(String userId);
    
    List<UserNotification> findByUserIdAndReadFalse(String userId);
    
    List<UserNotification> findByUserIdAndResolvedFalse(String userId);
    
    List<UserNotification> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
    
    List<UserNotification> findByType(String type);
    
    List<UserNotification> findByUserIdAndType(String userId, String type);
    
    @Query("SELECT u FROM UserNotification u WHERE u.userId = :userId AND u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<UserNotification> findRecentByUser(String userId, LocalDateTime since);
    
    long countByUserIdAndReadFalse(String userId);
    
    long countByUserIdAndResolvedFalse(String userId);
    
    long countByType(String type);
}
