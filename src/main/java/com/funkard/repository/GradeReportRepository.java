package com.funkard.repository;

import com.funkard.model.GradeReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GradeReportRepository extends JpaRepository<GradeReport, String> {
    List<GradeReport> findByExpiresAtBefore(LocalDateTime ts);
}