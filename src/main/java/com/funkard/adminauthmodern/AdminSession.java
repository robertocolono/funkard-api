package com.funkard.adminauthmodern;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 🔐 Entity per sessioni admin moderne (database-backed)
 */
@Entity
@Table(name = "admin_sessions", indexes = {
    @Index(name = "idx_admin_sessions_session_id", columnList = "session_id", unique = true),
    @Index(name = "idx_admin_sessions_admin_id", columnList = "admin_id"),
    @Index(name = "idx_admin_sessions_expires_at", columnList = "expires_at")
})
public class AdminSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", nullable = false, unique = true, length = 64)
    private String sessionId;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Costruttori
    public AdminSession() {}

    public AdminSession(String sessionId, UUID adminId, LocalDateTime expiresAt) {
        this.sessionId = sessionId;
        this.adminId = adminId;
        this.expiresAt = expiresAt;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getAdminId() {
        return adminId;
    }

    public void setAdminId(UUID adminId) {
        this.adminId = adminId;
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

