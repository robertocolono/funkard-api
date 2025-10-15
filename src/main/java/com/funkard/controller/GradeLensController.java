package com.funkard.controller;

import com.funkard.model.GradeLensResult;
import com.funkard.model.UserCard;
import com.funkard.model.dto.GradeRequest;
import com.funkard.repository.GradeLensRepository;
import com.funkard.repository.UserCardRepository;
import com.funkard.service.GradeCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/gradelens")
@CrossOrigin(origins = "https://funkard.vercel.app")
public class GradeLensController {

    private final GradeLensRepository repo;
    private final UserCardRepository userCardRepository;

    public GradeLensController(GradeLensRepository repo, UserCardRepository userCardRepository) {
        this.repo = repo;
        this.userCardRepository = userCardRepository;
    }
    // Endpoint deterministico: calcola grade e aggiorna UserCard
    @PostMapping("/analyze")
    public ResponseEntity<UserCard> analyzeAndSave(@RequestBody GradeRequest req) {
        var sub = GradeCalculator.computeSubgrades(req);
        double overall = GradeCalculator.computeOverall(sub);
        String label = GradeCalculator.labelFromGrade(overall);

        UserCard card = userCardRepository.findById(req.getCardId())
            .orElseThrow(() -> new RuntimeException("Card not found"));

        card.setGradeService(req.getService());
        card.setGradeOverall(overall);
        card.setGradeLabel(label);
        try {
            card.setSubgrades(new ObjectMapper().writeValueAsString(sub));
        } catch (Exception e) {
            card.setSubgrades("{}");
        }
        card.setGradedAt(java.time.LocalDateTime.now());
        userCardRepository.save(card);
        return ResponseEntity.ok(card);
    }

    @PostMapping
    public ResponseEntity<GradeLensResult> saveGrade(@RequestBody GradeLensResult data) {
        if (data.getGrade() < 0 || data.getGrade() > 10) {
            return ResponseEntity.badRequest().build();
        }
        // Set default source if missing to ensure cleanup rule applies
        if (data.getSource() == null || data.getSource().isBlank()) {
            data.setSource("gradelens");
        }
        // Initialize timestamps and flags if not provided
        if (data.getCreatedAt() == null) {
            data.setCreatedAt(LocalDateTime.now());
        }
        return ResponseEntity.ok(repo.save(data));
    }

    @GetMapping("/{userId}")
    public List<GradeLensResult> getByUser(@PathVariable String userId) {
        return repo.findByUserId(userId);
    }

    // Recupera grading per singola UserCard
    @GetMapping("/card/{userCardId}")
    public ResponseEntity<UserCard> getCardGrading(@PathVariable String userCardId) {
        return userCardRepository.findById(userCardId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<GradeLensResult> confirmAdd(@PathVariable Long id) {
        return repo.findById(id)
                .map(res -> {
                    res.setAddedToCollection(true);
                    return ResponseEntity.ok(repo.save(res));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanup() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(36);
        repo.deleteExpired("manual", cutoff);
        return ResponseEntity.ok("Cleanup eseguito per source=manual prima di " + cutoff);
    }
}