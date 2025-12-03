package com.funkard.adminauth.dto;

import java.util.UUID;

/**
 * ✅ Response DTO per validazione token onboarding
 */
public class TokenCheckResponse {
    private UUID id;
    private String role;
    private boolean onboardingCompleted;
    private String displayName;
    private String email;

    public TokenCheckResponse() {}

    public TokenCheckResponse(UUID id, String role, boolean onboardingCompleted, String displayName, String email) {
        this.id = id;
        this.role = role;
        this.onboardingCompleted = onboardingCompleted;
        this.displayName = displayName;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

