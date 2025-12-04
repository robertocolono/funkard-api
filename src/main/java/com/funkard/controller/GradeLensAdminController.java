package com.funkard.controller;

import com.funkard.repository.GradeReportRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/gradelens")
@CrossOrigin(origins = "*") // in produzione: limita ai tuoi domini o proteggi con auth
public class GradeLensAdminController {

    private final GradeReportRepository repo;

    public GradeLensAdminController(GradeReportRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Map<String, Object> metrics() {
        var now = LocalDateTime.now();
        var since24h = now.minusHours(24);

        long last24hTotal   = repo.countByCreatedAtAfter(since24h);
        long last24hAdShown = repo.countByAdShownTrueAndCreatedAtAfter(since24h);
        long active         = repo.countByExpiresAtAfter(now);
        long expired        = repo.countByExpiresAtBefore(now);

        return Map.of(
            "last24hTotal", last24hTotal,
            "last24hAdShown", last24hAdShown,
            "active", active,
            "expired", expired,
            "timestamp", now.toString()
        );
    }

    @PostMapping("/purge")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Map<String, Object> purgeExpired() {
        var now = LocalDateTime.now();
        var expired = repo.findByExpiresAtBefore(now);
        int n = expired.size();
        if (n > 0) repo.deleteAll(expired);
        return Map.of("purged", n, "timestamp", now.toString());
    }
}