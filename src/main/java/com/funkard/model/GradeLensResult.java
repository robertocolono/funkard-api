package com.funkard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "gradelens_results")
public class GradeLensResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private double centering;
    private double surface;
    private double edges;
    private double corners;
    private double grade; // overall grade
    private String category; // opzionale: categoria carta
    private String source;   // manual, purchase, gradelens
    private boolean verified = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean addedToCollection = false;
}
