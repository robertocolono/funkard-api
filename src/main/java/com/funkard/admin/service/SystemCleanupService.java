package com.funkard.admin.service;

import com.funkard.admin.model.SystemCleanupLog;
import com.funkard.admin.repository.SystemCleanupLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemCleanupService {

    private final SystemCleanupLogRepository repo;

    public void saveCleanupResult(String result, int deleted, String details) {
        SystemCleanupLog log = SystemCleanupLog.builder()
                .result(result)
                .deleted(deleted)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        repo.save(log);
    }

    public List<SystemCleanupLog> getAllLogs() {
        return repo.findAll();
    }

    public void clearOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);
        repo.findAll().stream()
                .filter(log -> log.getTimestamp().isBefore(cutoff))
                .forEach(repo::delete);
    }
}
