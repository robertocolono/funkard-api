package com.funkard.admin.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    @Value("${admin.token}")
    private String adminToken;

    @Value("${admin.email}")
    private String adminEmail;

    // Test: verifica se il token è valido
    @GetMapping("/ping")
    public ResponseEntity<String> ping(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok("Admin authorized: " + adminEmail);
    }
}
