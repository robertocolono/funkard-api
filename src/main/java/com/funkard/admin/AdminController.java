package com.funkard.admin;

import com.funkard.admin.dto.PendingItemDTO;
import com.funkard.adminauth.AdminUser;
import com.funkard.adminauth.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/valuation")
@CrossOrigin(origins = {"https://funkard.vercel.app", "http://localhost:3000"})
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminService service;
    private final AdminUserService userService;

    public AdminController(AdminService service, AdminUserService userService) {
        this.service = service;
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
            logger.warn("⚠️ Unauthorized access attempt on AdminController: no authentication in SecurityContext");
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
            logger.warn("⚠️ Unauthorized access attempt on AdminController: unable to extract email from principal");
            return null;
        }
        
        // Trova admin per email
        AdminUser admin = userService.getByEmail(email);
        if (admin == null) {
            logger.warn("⚠️ Unauthorized access attempt on AdminController: admin not found for email: {}", email);
            return null;
        }
        
        logger.debug("✅ Admin authenticated: {} ({})", admin.getDisplayName() != null ? admin.getDisplayName() : admin.getName(), email);
        return admin;
    }

    /**
     * 📋 GET /api/admin/valuation/pending
     * Ottiene lista di item pending per valutazione
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> getPendingItems() {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser admin = getCurrentAdmin();
        if (admin == null) {
            logger.warn("🚫 Legacy token static check removed: getPendingItems requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        logger.debug("✅ Admin {} accessed pending items", admin.getEmail());
        return ResponseEntity.ok(service.getPendingItems());
    }
    
    /**
     * ✅ GET /api/admin/valuation/check
     * Verifica autenticazione admin (endpoint di test)
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> checkAdmin() {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser admin = getCurrentAdmin();
        if (admin == null) {
            logger.warn("🚫 Legacy token static check removed: checkAdmin requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        
        logger.debug("✅ Admin {} checked authentication", admin.getEmail());
        return ResponseEntity.ok("Access granted");
    }
}
