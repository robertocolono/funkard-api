package com.funkard.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminConfig {
    @Value("${admin.token}")
    private String adminToken;

    public String getAdminToken() {
        return adminToken;
    }
}
