package com.funkard.adminauth;

import com.funkard.adminaccess.model.AdminAccessToken;
import com.funkard.adminaccess.repository.AdminAccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 🔑 Controller per gestione token di accesso admin
 */
@RestController
@RequestMapping("/api/admin/tokens")
@CrossOrigin(origins = {"https://funkard.com", "https://www.funkard.com", "http://localhost:3002"})
@RequiredArgsConstructor
public class AdminTokenController {

    private final AdminAccessTokenRepository tokenRepository;

    /**
     * 📋 GET /api/admin/tokens
     * Lista tutti i token
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> listTokens() {
        return ResponseEntity.ok(tokenRepository.findAll());
    }

    /**
     * ➕ POST /api/admin/tokens/generate
     * Genera un nuovo token per un ruolo
     * Query param: role
     * Header: Authorization (deve contenere SUPER_ADMIN_TOKEN)
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> generateToken(
            @RequestParam String role,
            @RequestHeader("Authorization") String authHeader) {
        
        String superAdminToken = System.getenv("SUPER_ADMIN_TOKEN");
        if (superAdminToken == null || !authHeader.contains(superAdminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accesso non autorizzato"));
        }

        // Genera token UUID senza trattini
        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        
        AdminAccessToken token = AdminAccessToken.builder()
                .role(role)
                .token(tokenValue)
                .createdBy("RootSuperAdmin")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        tokenRepository.save(token);
        
        return ResponseEntity.ok(Map.of(
            "token", tokenValue,
            "role", role
        ));
    }

    /**
     * 🔍 GET /api/admin/tokens/validate/{token}
     * Valida un token
     */
    @GetMapping("/validate/{token}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> validateToken(@PathVariable String token) {
        return tokenRepository.findByToken(token)
                .map(t -> ResponseEntity.ok(Map.of(
                    "valid", true,
                    "role", t.getRole()
                )))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false)));
    }
}
