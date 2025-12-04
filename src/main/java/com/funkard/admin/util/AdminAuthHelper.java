package com.funkard.admin.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 🔐 Helper per autenticazione admin
 * Supporta sia Authorization: Bearer {token} che X-Admin-Token (legacy)
 */
@Component
public class AdminAuthHelper {

    @Value("${admin.token:}")
    private String adminToken;

    @Value("${SUPER_ADMIN_TOKEN:}")
    private String superAdminToken;

    /**
     * Valida autenticazione admin
     * @param authHeader Header Authorization (Bearer token)
     * @param adminTokenHeader Header X-Admin-Token (legacy)
     * @return true se autenticato, false altrimenti
     */
    public boolean validateAdminAuth(String authHeader, String adminTokenHeader) {
        // Verifica X-Admin-Token (legacy)
        if (adminTokenHeader != null && !adminTokenHeader.trim().isEmpty()) {
            String token = adminTokenHeader.trim();
            if (adminToken != null && !adminToken.isEmpty() && token.equals(adminToken)) {
                return true;
            }
            if (superAdminToken != null && !superAdminToken.isEmpty() && token.equals(superAdminToken)) {
                return true;
            }
        }

        // Verifica Authorization: Bearer {token}
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (adminToken != null && !adminToken.isEmpty() && token.equals(adminToken)) {
                return true;
            }
            if (superAdminToken != null && !superAdminToken.isEmpty() && token.equals(superAdminToken)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Restituisce risposta 403 se non autenticato
     */
    public ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Accesso non autorizzato. Token admin richiesto."));
    }
}


