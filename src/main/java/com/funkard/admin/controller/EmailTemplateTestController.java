package com.funkard.admin.controller;

import com.funkard.service.EmailTemplateTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 🧪 Controller Admin per test template email
 * 
 * Accesso: Solo SUPER_ADMIN
 */
@RestController
@RequestMapping("/api/admin/email-templates/test")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "https://admin.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
})
public class EmailTemplateTestController {
    
    private final EmailTemplateTestService testService;
    
    /**
     * 🧪 POST /api/admin/email-templates/test/all
     * Esegue test completo su tutti i template e lingue
     */
    @PostMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> testAllTemplates() {
        log.info("Richiesta test completo template email");
        
        try {
            testService.testAllTemplates();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Test completato. Verifica i log per dettagli."
            ));
        } catch (Exception e) {
            log.error("Errore durante test template: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 🧪 POST /api/admin/email-templates/test/variables
     * Test sostituzione variabili
     */
    @PostMapping("/variables")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> testVariableSubstitution() {
        log.info("Richiesta test sostituzione variabili");
        
        try {
            boolean success = testService.testVariableSubstitution();
            return ResponseEntity.ok(Map.of(
                "status", success ? "success" : "warning",
                "message", success ? "Sostituzione variabili funzionante" : "Problemi rilevati",
                "result", success
            ));
        } catch (Exception e) {
            log.error("Errore durante test variabili: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
}

