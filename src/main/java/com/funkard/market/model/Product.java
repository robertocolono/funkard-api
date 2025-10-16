package com.funkard.market.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private Double price;
    
    private Double estimatedValue;
    
    @Column(name = "user_id")
    private String userId;
    
    // Constructors, getters, setters handled by Lombok @Data
}
