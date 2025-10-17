package com.funkard.admin.controller;

import com.funkard.admin.model.AdminActionLog;
import com.funkard.admin.repository.AdminActionLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/actions")
@CrossOrigin(origins = {
    "https://funkard-admin.vercel.app",
    "http://localhost:3000"
}, allowCredentials = "true")
public class AdminActionLogController {

    private final AdminActionLogRepository logRepository;

    public AdminActionLogController(AdminActionLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Ottiene tutti i log delle azioni admin
     */
    @GetMapping
    public ResponseEntity<List<AdminActionLog>> getAllActions() {
        List<AdminActionLog> actions = logRepository.findAll();
        return ResponseEntity.ok(actions);
    }

    /**
     * Ottiene i log per una specifica entità (notifica, ticket, ecc.)
     */
    @GetMapping("/target/{targetId}/{targetType}")
    public ResponseEntity<List<AdminActionLog>> getActionsByTarget(
            @PathVariable Long targetId, 
            @PathVariable String targetType) {
        List<AdminActionLog> actions = logRepository.findByTargetIdAndTargetTypeOrderByCreatedAtAsc(targetId, targetType);
        return ResponseEntity.ok(actions);
    }

    /**
     * Ottiene i log per tipo di entità
     */
    @GetMapping("/type/{targetType}")
    public ResponseEntity<List<AdminActionLog>> getActionsByType(@PathVariable String targetType) {
        List<AdminActionLog> actions = logRepository.findByTargetTypeOrderByCreatedAtDesc(targetType);
        return ResponseEntity.ok(actions);
    }

    /**
     * Ottiene i log per admin specifico
     */
    @GetMapping("/admin/{performedBy}")
    public ResponseEntity<List<AdminActionLog>> getActionsByAdmin(@PathVariable String performedBy) {
        List<AdminActionLog> actions = logRepository.findByPerformedByOrderByCreatedAtDesc(performedBy);
        return ResponseEntity.ok(actions);
    }

    /**
     * Ottiene i log per tipo di azione
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<List<AdminActionLog>> getActionsByAction(@PathVariable String action) {
        List<AdminActionLog> actions = logRepository.findByActionOrderByCreatedAtDesc(action);
        return ResponseEntity.ok(actions);
    }
}
