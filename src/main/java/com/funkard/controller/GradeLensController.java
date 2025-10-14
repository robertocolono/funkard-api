package com.funkard.controller;

import com.funkard.model.GradeLensResult;
import com.funkard.service.GradeLensService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/gradelens")
@CrossOrigin(origins = "https://funkard.vercel.app")
public class GradeLensController {

    private final GradeLensService gradeLensService;

    public GradeLensController(GradeLensService gradeLensService) {
        this.gradeLensService = gradeLensService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeCard(
            @RequestParam String userCardId,
            @RequestParam MultipartFile frontImage,
            @RequestParam MultipartFile backImage,
            @RequestParam(required = false) List<MultipartFile> extraImages
    ) {
        try {
            GradeLensResult result = gradeLensService.analyzeCard(userCardId, frontImage, backImage, extraImages);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Errore durante l'analisi GradeLens");
        }
    }

    @GetMapping("/{userCardId}")
    public ResponseEntity<List<GradeLensResult>> getResults(@PathVariable String userCardId) {
        return ResponseEntity.ok(gradeLensService.getResultsByUserCard(userCardId));
    }
}