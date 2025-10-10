package com.funkard.maintenance;

import com.funkard.repository.GradeReportRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class GradeReportCleanup {

    private final GradeReportRepository repo;

    public GradeReportCleanup(GradeReportRepository repo) {
        this.repo = repo;
    }

    // Esegue ogni notte alle 03:15
    @Scheduled(cron = "0 15 3 * * *", zone = "Europe/Rome")
    public void purgeExpired() {
        var now = LocalDateTime.now();
        var expired = repo.findByExpiresAtBefore(now);
        if (!expired.isEmpty()) {
            repo.deleteAll(expired);
        }
    }
}