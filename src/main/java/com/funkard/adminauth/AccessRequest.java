package com.funkard.adminauth;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 📝 Entity per richieste di accesso admin
 */
@Entity
@Table(name = "access_requests", indexes = {
    @Index(name = "idx_access_requests_email", columnList = "email"),
    @Index(name = "idx_access_requests_status", columnList = "status"),
    @Index(name = "idx_access_requests_token", columnList = "token_used")
})
public class AccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(name = "token_used", nullable = false, length = 256)
    private String tokenUsed;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // APPROVED / REJECTED / PENDING

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "approved_by")
    private UUID approvedBy;

    // Costruttori
    public AccessRequest() {}

    public AccessRequest(String email, String role, String tokenUsed) {
        this.email = email;
        this.role = role;
        this.tokenUsed = tokenUsed;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getTokenUsed() {
        return tokenUsed;
    }

    public void setTokenUsed(String tokenUsed) {
        this.tokenUsed = tokenUsed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
    }
}

