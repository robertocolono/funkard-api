package com.funkard.admin.controller;

import com.funkard.adminauth.AdminUser;
import com.funkard.adminauth.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 🔐 Controller per endpoint ping admin (modernizzato)
 * Sostituisce il vecchio sistema legacy basato su token statici
 * Usa autenticazione moderna basata su sessioni httpOnly
 */
@RestController
@RequestMapping("/api/admin")
public class AdminLegacyAuthController {

    private static final Logger logger = LoggerFactory.getLogger(AdminLegacyAuthController.class);

    private final AdminUserService userService;

    public AdminLegacyAuthController(AdminUserService userService) {
        this.userService = userService;
    }

    /**
     * 🔐 Helper: Recupera admin corrente da SecurityContext
     * Usa autenticazione moderna basata su sessioni httpOnly
     * @return AdminUser corrente o null se non autenticato
     */
    private AdminUser getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("⚠️ Unauthorized access attempt on AdminLegacyAuthController: no authentication in SecurityContext");
            return null;
        }
        
        // Estrai email da principal
        Object principal = authentication.getPrincipal();
        String email = null;
        
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        }
        
        if (email == null || email.trim().isEmpty()) {
            logger.warn("⚠️ Unauthorized access attempt on AdminLegacyAuthController: unable to extract email from principal");
            return null;
        }
        
        // Trova admin per email
        AdminUser admin = userService.getByEmail(email);
        if (admin == null) {
            logger.warn("⚠️ Unauthorized access attempt on AdminLegacyAuthController: admin not found for email: {}", email);
            return null;
        }
        
        logger.debug("✅ Admin authenticated: {} ({})", admin.getDisplayName() != null ? admin.getDisplayName() : admin.getName(), email);
        return admin;
    }

    /**
     * ✅ GET /api/admin/ping
     * Endpoint di test per verificare autenticazione admin
     * Usa autenticazione moderna basata su sessioni httpOnly
     * Sostituisce il vecchio endpoint legacy che usava token statici
     */
    @GetMapping("/ping")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<String> ping() {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser admin = getCurrentAdmin();
        if (admin == null) {
            logger.warn("🚫 Legacy token static check removed: ping requires modern session authentication");
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        logger.debug("✅ Admin {} pinged authentication endpoint", admin.getEmail());
        return ResponseEntity.ok("Admin authorized: " + admin.getEmail());
    }
}
