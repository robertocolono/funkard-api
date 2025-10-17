package com.funkard.admin.repository;

import com.funkard.admin.model.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
    
    List<AdminActionLog> findByTargetIdAndTargetTypeOrderByCreatedAtAsc(Long targetId, String targetType);
    
    List<AdminActionLog> findByTargetTypeOrderByCreatedAtDesc(String targetType);
    
    List<AdminActionLog> findByPerformedByOrderByCreatedAtDesc(String performedBy);
    
    List<AdminActionLog> findByActionOrderByCreatedAtDesc(String action);
}
