package com.funkard.gradelens.controller;

import com.funkard.gradelens.model.GradeLensResponse;
import com.funkard.gradelens.service.GradeLensService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gradelens")
@CrossOrigin(origins = {"https://funkard.vercel.app", "https://funkardnew.vercel.app"})
public class GradeLensController {

    private final GradeLensService service;

    public GradeLensController(GradeLensService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public ResponseEntity<GradeLensResponse> analyze(@RequestBody Map<String, String> body) {
        String front = body.get("frontImageUrl");
        String back = body.get("backImageUrl");
        return ResponseEntity.ok(service.analyze(front, back));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@RequestBody Map<String, Object> body) {
        try {
            String userId = (String) body.get("userId");
            String name = (String) body.get("name");
            String setName = (String) body.get("setName");
            String condition = (String) body.get("condition");
            String frontImageUrl = (String) body.get("frontImageUrl");
            String backImageUrl = (String) body.get("backImageUrl");

            @SuppressWarnings("unchecked")
            Map<String, Object> subgrades = (Map<String, Object>) body.get("subgrades");
            @SuppressWarnings("unchecked")
            Map<String, Object> analysisMeta = (Map<String, Object>) body.get("analysisMeta");
            @SuppressWarnings("unchecked")
            java.util.List<String> diagnostics = (java.util.List<String>) body.get("diagnostics");
            Double overallGrade = ((Number) body.get("overallGrade")).doubleValue();

            String id = service.saveGradedCard(
                    userId, name, setName, condition,
                    frontImageUrl, backImageUrl,
                    subgrades, overallGrade, analysisMeta, diagnostics
            );

            return ResponseEntity.ok(Map.of("status", "saved", "userCardId", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
