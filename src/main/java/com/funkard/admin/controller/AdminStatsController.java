package com.funkard.admin.controller;

import com.funkard.admin.dto.AdminStatsDTO;
import com.funkard.admin.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 📊 Controller per statistiche admin
 * Richiede autenticazione JWT con ruolo ADMIN o SUPER_ADMIN
 */
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"https://admin.funkard.com", "https://funkard.com", "https://www.funkard.com", "http://localhost:3000", "http://localhost:3002"})
public class AdminStatsController {

    private final AdminStatsService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminStatsDTO> getStats() {
        log.info("📊 Richiesta statistiche admin");
        AdminStatsDTO stats = service.getStats();
        return ResponseEntity.ok(stats);
    }
}
