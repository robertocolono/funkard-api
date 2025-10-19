package com.funkard.admin.log;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
    List<AdminActionLog> findByTargetIdAndTargetTypeOrderByCreatedAtAsc(Long targetId, String targetType);
}
