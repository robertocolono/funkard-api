package com.funkard.controller;

import com.funkard.gradelens.GradeLensService;
import com.funkard.model.GradeReport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gradelens")
@CrossOrigin(origins = "*") // in prod limita a funkard.vercel.app
public class GradeLensController {

    private final GradeLensService service;

    public GradeLensController(GradeLensService service) {
        this.service = service;
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

    // (Opzionale) Recupera un report per id — valido finché non scade
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        return ResponseEntity.ofNullable(null); // puoi implementare se ti serve
    }
}