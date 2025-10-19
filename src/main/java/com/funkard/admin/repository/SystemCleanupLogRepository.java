package com.funkard.admin.repository;

import com.funkard.admin.model.SystemCleanupLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemCleanupLogRepository extends JpaRepository<SystemCleanupLog, Long> {
}
