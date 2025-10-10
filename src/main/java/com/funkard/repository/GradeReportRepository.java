package com.funkard.repository;

import com.funkard.model.GradeReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GradeReportRepository extends JpaRepository<GradeReport, String> {
    List<GradeReport> findByExpiresAtBefore(LocalDateTime ts);

    long countByCreatedAtAfter(LocalDateTime since);                    // total last 24h
    long countByAdShownTrueAndCreatedAtAfter(LocalDateTime since);      // ad impressions last 24h
    long countByExpiresAtAfter(LocalDateTime now);                      // active (not expired yet)
    long countByExpiresAtBefore(LocalDateTime now);                     // expired (up to now)
}