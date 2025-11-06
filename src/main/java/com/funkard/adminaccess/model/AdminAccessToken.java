package com.funkard.adminaccess.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 🔑 Entity per token di accesso admin
 */
@Entity
@Table(name = "admin_access_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

