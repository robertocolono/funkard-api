package com.funkard.adminauth.dto;

/**
 * 📝 Request DTO per completamento onboarding admin
 */
public class OnboardingCompleteRequest {
    private String token;
    private String email;
    private String password;
    private String displayName;

    public OnboardingCompleteRequest() {}

    public OnboardingCompleteRequest(String token, String email, String password, String displayName) {
        this.token = token;
        this.email = email;
        this.password = password;
        this.displayName = displayName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

