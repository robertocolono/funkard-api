package com.funkard.admin.controller;

import com.funkard.admin.dto.MarketOverviewDTO;
import com.funkard.admin.service.AdminValuationService;
import com.funkard.adminauth.AdminUser;
import com.funkard.adminauth.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/valuation")
@CrossOrigin(origins = {"https://admin.funkard.com", "https://funkard.vercel.app"})
public class AdminValuationController {

    private static final Logger logger = LoggerFactory.getLogger(AdminValuationController.class);

    private final AdminValuationService service;
    private final AdminUserService userService;

    public AdminValuationController(AdminValuationService service, AdminUserService userService) {
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
            logger.warn("⚠️ Unauthorized access attempt on AdminValuationController: no authentication in SecurityContext");
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
            logger.warn("⚠️ Unauthorized access attempt on AdminValuationController: unable to extract email from principal");
            return null;
        }
        
        // Trova admin per email
        AdminUser admin = userService.getByEmail(email);
        if (admin == null) {
            logger.warn("⚠️ Unauthorized access attempt on AdminValuationController: admin not found for email: {}", email);
            return null;
        }
        
        logger.debug("✅ Admin authenticated: {} ({})", admin.getDisplayName() != null ? admin.getDisplayName() : admin.getName(), email);
        return admin;
    }

    /**
     * 📊 GET /api/admin/valuation/overview
     * Ottiene overview del mercato degli ultimi 7 giorni
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> getOverview() {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser admin = getCurrentAdmin();
        if (admin == null) {
            logger.warn("🚫 Legacy token static check removed: getOverview requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        logger.info("✅ Admin {} accessed valuation overview", admin.getEmail());
        List<MarketOverviewDTO> overview = service.getOverviewLast7Days();
        return ResponseEntity.ok(overview);
    }
}
