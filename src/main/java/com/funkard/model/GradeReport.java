package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class GradeReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // input
    private String imageUrl;

    // punteggi
    private double grade;
    private double centering;
    private double edges;
    private double corners;
    private double surface;

    // stima valore
    private double valueLow;
    private double valueMid;
    private double valueHigh;
    private String currency;

    // meta
    private String mode;           // "heuristic" (per ora)
    private String notes;

    private Boolean adShown = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt = createdAt.plusHours(48);
}