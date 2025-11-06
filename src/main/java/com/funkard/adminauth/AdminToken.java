package com.funkard.adminauth;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 🔑 Entity per token di ruolo admin
 */
@Entity
@Table(name = "admin_tokens", indexes = {
    @Index(name = "idx_admin_tokens_token", columnList = "token"),
    @Index(name = "idx_admin_tokens_role", columnList = "role"),
    @Index(name = "idx_admin_tokens_active", columnList = "active")
})
public class AdminToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String role; // ADMIN, SUPERVISOR, SUPER_ADMIN

    @Column(nullable = false, unique = true, length = 256)
    private String token;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Costruttori
    public AdminToken() {}

    public AdminToken(String role, String token, UUID createdBy) {
        this.role = role;
        this.token = token;
        this.createdBy = createdBy;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}

