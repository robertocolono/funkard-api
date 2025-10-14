package com.funkard.controller;

import com.funkard.model.GradeLensResult;
import com.funkard.repository.GradeLensRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/gradelens")
@CrossOrigin(origins = "https://funkard.vercel.app")
public class GradeLensController {

    private final GradeLensRepository repo;

    public GradeLensController(GradeLensRepository repo) {
        this.repo = repo;
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
        int deleted = repo.deleteExpired("gradelens", cutoff);
        return ResponseEntity.ok("Deleted " + deleted + " expired results");
    }
}