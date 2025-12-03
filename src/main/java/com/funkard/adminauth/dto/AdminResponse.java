package com.funkard.adminauth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 👤 Response DTO per dati admin (usato in onboarding-complete, login, me)
 */
public class AdminResponse {
    private UUID id;
    private String email;
    private String displayName;
    private String role;
    private boolean isRoot;
    private LocalDateTime lastLoginAt;

    public AdminResponse() {}

    public AdminResponse(UUID id, String email, String displayName, String role, boolean isRoot, LocalDateTime lastLoginAt) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.role = role;
        this.isRoot = isRoot;
        this.lastLoginAt = lastLoginAt;
    }

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}

