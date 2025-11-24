package com.funkard.adminauth;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 👤 Entity per utenti admin del pannello Funkard
 */
@Entity
@Table(name = "admin_users", indexes = {
    @Index(name = "idx_admin_users_email", columnList = "email"),
    @Index(name = "idx_admin_users_token", columnList = "access_token"),
    @Index(name = "idx_admin_users_role", columnList = "role")
})
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String role; // "SUPER_ADMIN", "SUPERVISOR", "ADMIN"

    @Column(name = "access_token", nullable = false, unique = true, length = 128)
    private String accessToken;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "is_root", nullable = false)
    private boolean isRoot = false;

    @Column(nullable = true)
    private Boolean pending = false;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Costruttori
    public AdminUser() {}

    public AdminUser(String name, String email, String role, String accessToken) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.accessToken = accessToken;
        this.active = true;
        this.isRoot = false;
    }

    public AdminUser(String name, String email, String role, String accessToken, boolean isRoot) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.accessToken = accessToken;
        this.active = true;
        this.isRoot = isRoot;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public Boolean isPending() {
        return pending;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}

