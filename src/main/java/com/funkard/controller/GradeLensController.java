package com.funkard.controller;

import com.funkard.gradelens.GradeLensService;
import com.funkard.model.GradeReport;
import com.funkard.service.GradeReportLookupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gradelens")
@CrossOrigin(origins = "*") // in prod limita a funkard.vercel.app
public class GradeLensController {

    private final GradeLensService service;
    private final GradeReportLookupService lookupService;

    public GradeLensController(GradeLensService service, GradeReportLookupService lookupService) {
        this.service = service;
        this.lookupService = lookupService;
    }

    // Analizza e salva report (ritorna l'oggetto salvato, con id)
    @PostMapping
    public ResponseEntity<?> analyze(@RequestBody Map<String, Object> body) {
        String imageUrl = body.get("imageUrl") != null ? body.get("imageUrl").toString() : null;
        boolean adShown = body.get("adShown") != null && Boolean.parseBoolean(body.get("adShown").toString());

        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "imageUrl is required"));
        }
        GradeReport saved = service.analyzeAndStore(imageUrl, adShown);
        return ResponseEntity.ok(saved);
    }

    // Recupera un report per id — valido finché non scade
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            var report = lookupService.getOrGone(id);
            return ResponseEntity.ok(report);
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("error", ex.getReason()));
        }
    }
}