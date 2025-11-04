package com.funkard.admin.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @deprecated Questo controller è legacy. Usa il nuovo sistema in com.funkard.adminauth.AdminAuthController
 * Mantenuto solo per compatibilità con endpoint /api/admin/ping
 */
@RestController
@RequestMapping("/api/admin")
@Deprecated
public class AdminLegacyAuthController {

    @Value("${admin.token}")
    private String adminToken;

    @Value("${admin.email}")
    private String adminEmail;

    // Test: verifica se il token è valido (legacy endpoint)
    @GetMapping("/ping")
    public ResponseEntity<String> ping(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok("Admin authorized: " + adminEmail);
    }
}
