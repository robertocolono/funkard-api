package com.funkard.grading.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "grading_requests")
@Data
public class GradingRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "card_id", nullable = false)
    private Long cardId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED, FAILED
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    // Constructors, getters, setters handled by Lombok @Data
}
