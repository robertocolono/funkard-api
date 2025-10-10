package com.funkard.service;

import com.funkard.model.GradeReport;
import com.funkard.repository.GradeReportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class GradeReportLookupService {

    private final GradeReportRepository repo;

    public GradeReportLookupService(GradeReportRepository repo) {
        this.repo = repo;
    }

    public GradeReport getOrGone(String id) {
        var report = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report non trovato"));
        if (report.getExpiresAt() != null && report.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Report scaduto");
        }
        return report;
    }
}