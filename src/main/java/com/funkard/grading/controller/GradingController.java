package com.funkard.grading.controller;

import com.funkard.grading.model.GradingRequest;
import com.funkard.grading.service.GradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grading")
@CrossOrigin(origins = "*")
public class GradingController {

    private final GradingService gradingService;

    public GradingController(GradingService gradingService) {
        this.gradingService = gradingService;
    }

    @PostMapping("/submit")
    public ResponseEntity<GradingRequest> submitForGrading(@RequestBody GradingRequest request) {
        try {
            gradingService.sendForGrading(request);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            // L'errore viene gestito automaticamente dal GradingService
            // che crea la notifica admin
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{cardId}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long cardId, @RequestBody String status) {
        try {
            gradingService.updateGradingStatus(cardId, status);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // L'errore viene gestito automaticamente dal GradingService
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{cardId}/failed")
    public ResponseEntity<Void> markAsFailed(@PathVariable Long cardId, @RequestBody String errorMessage) {
        try {
            gradingService.markGradingAsFailed(cardId, errorMessage);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // L'errore viene gestito automaticamente dal GradingService
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{cardId}/completed")
    public ResponseEntity<Void> markAsCompleted(@PathVariable Long cardId, @RequestBody String result) {
        try {
            gradingService.markGradingAsCompleted(cardId, result);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // L'errore viene gestito automaticamente dal GradingService
            return ResponseEntity.internalServerError().build();
        }
    }
}
