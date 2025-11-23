package com.funkard.admin.controller;

import com.funkard.model.EmailLog;
import com.funkard.repository.EmailLogRepository;
import com.funkard.service.EmailLogService;
import com.funkard.service.EmailTemplateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 📧 Controller Admin per audit email logs
 * 
 * Accesso: Solo SUPER_ADMIN e SUPERVISOR
 */
@RestController
@RequestMapping("/api/admin/email-logs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "https://admin.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
})
public class AdminEmailLogController {
    
    private final EmailLogRepository logRepository;
    private final EmailLogService logService;
    private final EmailTemplateManager templateManager;
    
    /**
     * 📋 GET /api/admin/email-logs
     * Elenco paginato log email con filtri
     * 
     * Query params:
     * - recipient: filtra per destinatario
     * - type: filtra per tipo email
     * - status: filtra per stato (SENT, FAILED, RETRIED)
     * - fromDate: data inizio (ISO format)
     * - toDate: data fine (ISO format)
     * - page: numero pagina (default 0)
     * - size: dimensione pagina (default 20)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Page<EmailLog>> getEmailLogs(
            @RequestParam(required = false) String recipient,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) EmailLog.EmailStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Richiesta log email - recipient: {}, type: {}, status: {}", recipient, type, status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        
        Page<EmailLog> logs = logRepository.findWithFilters(
            recipient, type, status, fromDate, toDate, pageable
        );
        
        return ResponseEntity.ok(logs);
    }
    
    /**
     * 🔍 GET /api/admin/email-logs/{id}
     * Dettaglio singolo log email
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPERVISOR')")
    public ResponseEntity<?> getEmailLogDetail(@PathVariable UUID id) {
        log.info("Richiesta dettaglio log email: {}", id);
        
        EmailLog emailLog = logService.findById(id);
        if (emailLog == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Ricostruisci contenuto email se template disponibile
        Map<String, Object> response = new HashMap<>();
        response.put("log", emailLog);
        
        if (emailLog.getTemplateName() != null) {
            try {
                // Prova a renderizzare template con variabili di esempio
                Map<String, Object> exampleVars = new HashMap<>();
                exampleVars.put("username", "Example User");
                exampleVars.put("date", LocalDateTime.now().toString());
                
                Locale locale = new Locale(emailLog.getLocale());
                String renderedHtml = templateManager.renderHtmlTemplate(
                    emailLog.getTemplateName(), exampleVars, locale);
                
                response.put("renderedContent", renderedHtml);
                response.put("templateName", emailLog.getTemplateName());
            } catch (Exception e) {
                log.warn("Errore renderizzazione template {}: {}", emailLog.getTemplateName(), e.getMessage());
            }
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 📊 GET /api/admin/email-logs/stats
     * Statistiche email logs
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getEmailLogStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        
        log.info("Richiesta statistiche email logs");
        
        LocalDateTime start = fromDate != null ? fromDate : LocalDateTime.now().minusDays(30);
        LocalDateTime end = toDate != null ? toDate : LocalDateTime.now();
        
        Pageable pageable = PageRequest.of(0, 1);
        
        Page<EmailLog> allLogs = logRepository.findWithFilters(null, null, null, start, end, pageable);
        Page<EmailLog> sentLogs = logRepository.findWithFilters(null, null, EmailLog.EmailStatus.SENT, start, end, pageable);
        Page<EmailLog> failedLogs = logRepository.findWithFilters(null, null, EmailLog.EmailStatus.FAILED, start, end, pageable);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allLogs.getTotalElements());
        stats.put("sent", sentLogs.getTotalElements());
        stats.put("failed", failedLogs.getTotalElements());
        stats.put("retried", logRepository.findWithFilters(null, null, EmailLog.EmailStatus.RETRIED, start, end, pageable).getTotalElements());
        stats.put("period", Map.of("from", start, "to", end));
        
        return ResponseEntity.ok(stats);
    }
}

