package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String setName;
    private String rarity;
    private Integer grade;
    private String imageUrl;
    private Double marketValue = 0.0;
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "card")
    private List<Listing> listings;
}
