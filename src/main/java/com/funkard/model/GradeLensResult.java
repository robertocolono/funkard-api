package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "gradelens_results")
public class GradeLensResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userCardId;

    private Double corners;     // 0–10
    private Double edges;       // 0–10
    private Double surface;     // 0–10
    private Double centering;   // 0–10
    private Double overallGrade; // media ponderata finale

    private String aiModel;     // es. "Funkard-GradeLens-v1"
    private String source;      // manual, AI, hybrid

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userCardId", insertable = false, updatable = false)
    private UserCard userCard;
}
