package com.funkard.admin.controller;

import com.funkard.admin.service.RolePermissionService;
import com.funkard.adminauth.AdminUser;
import com.funkard.adminauth.AdminUserService;
import com.funkard.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 🔐 Controller per gestione ruoli e permessi
 */
@RestController
@RequestMapping("/api/admin/roles")
@CrossOrigin(origins = {"https://funkard-admin.vercel.app", "http://localhost:3000"})
public class RolePermissionController {

    private static final Logger logger = LoggerFactory.getLogger(RolePermissionController.class);

    @Autowired
    private RolePermissionService roleService;

    @Autowired
    private AdminUserService userService;

    /**
     * 🔐 Helper: Recupera admin corrente da SecurityContext
     * Usa autenticazione moderna basata su sessioni httpOnly
     * @return AdminUser corrente o null se non autenticato
     */
    private AdminUser getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("⚠️ Unauthorized access attempt on RolePermissionController: no authentication in SecurityContext");
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
            logger.warn("⚠️ Unauthorized access attempt on RolePermissionController: unable to extract email from principal");
            return null;
        }
        
        // Trova admin per email
        AdminUser admin = userService.getByEmail(email);
        if (admin == null) {
            logger.warn("⚠️ Unauthorized access attempt on RolePermissionController: admin not found for email: {}", email);
            return null;
        }
        
        logger.debug("✅ Admin authenticated: {} ({})", admin.getDisplayName() != null ? admin.getDisplayName() : admin.getName(), email);
        return admin;
    }

    /**
     * 📊 Ottieni permessi per un utente
     * GET /api/admin/roles/permissions/{userEmail}
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @GetMapping("/permissions/{userEmail}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> getUserPermissions(@PathVariable String userEmail) {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser admin = getCurrentAdmin();
        if (admin == null) {
            logger.warn("🚫 Legacy token static check removed: getUserPermissions requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            logger.info("✅ Admin {} accessed role permissions for user: {}", admin.getEmail(), userEmail);
            // TODO: Implementare ricerca utente reale
            User user = createMockUser(userEmail);
            Map<String, Object> permissions = roleService.getUserPermissions(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "email", user.getEmail(),
                    "role", user.getRole()
                ),
                "permissions", permissions
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore durante il recupero permessi: " + e.getMessage()));
        }
    }

    /**
     * 🔍 Verifica permessi specifici per un ticket
     * POST /api/admin/roles/check-permissions
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @PostMapping("/check-permissions")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> checkTicketPermissions(@RequestBody Map<String, Object> request) {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser admin = getCurrentAdmin();
        if (admin == null) {
            logger.warn("🚫 Legacy token static check removed: checkTicketPermissions requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String userEmail = (String) request.get("userEmail");
            String ticketId = (String) request.get("ticketId");
            String action = (String) request.get("action");
            
            // TODO: Implementare ricerca ticket e utente reali
            User user = createMockUser(userEmail);
            
            Map<String, Boolean> permissions = Map.of(
                "canView", true, // TODO: Implementare logica reale
                "canModify", true,
                "canAssign", true,
                "canUnassign", true,
                "canClose", true,
                "canReply", true
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "userEmail", userEmail,
                "ticketId", ticketId,
                "action", action,
                "permissions", permissions
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore durante la verifica permessi: " + e.getMessage()));
        }
    }

    /**
     * 📋 Lista tutti i ruoli disponibili
     * GET /api/admin/roles/available
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> getAvailableRoles() {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser admin = getCurrentAdmin();
        if (admin == null) {
            logger.warn("🚫 Legacy token static check removed: getAvailableRoles requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "roles", Map.of(
                    "SUPER_ADMIN", Map.of(
                        "code", "SUPER_ADMIN",
                        "description", "Super Admin",
                        "level", 3,
                        "permissions", Map.of(
                            "canViewAllTickets", true,
                            "canModifyAnyTicket", true,
                            "canAssignAnyTicket", true,
                            "canUnlockAnyTicket", true,
                            "canCloseAnyTicket", true
                        )
                    ),
                    "ADMIN", Map.of(
                        "code", "ADMIN",
                        "description", "Admin",
                        "level", 2,
                        "permissions", Map.of(
                            "canViewAllTickets", true,
                            "canModifyOpenTickets", true,
                            "canAssignOpenTickets", true,
                            "canUnlockAnyTicket", true,
                            "canCloseOpenTickets", true
                        )
                    ),
                    "SUPPORT", Map.of(
                        "code", "SUPPORT",
                        "description", "Support",
                        "level", 1,
                        "permissions", Map.of(
                            "canViewOwnTickets", true,
                            "canModifyOwnTickets", true,
                            "canAssignOpenTickets", true,
                            "canUnlockOwnTickets", true,
                            "canCloseOwnTickets", true
                        )
                    )
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore durante il recupero ruoli: " + e.getMessage()));
        }
    }

    /**
     * 🔧 Crea User mock per testing
     */
    private User createMockUser(String userEmail) {
        User user = new User();
        user.setId(1L);
        user.setEmail(userEmail);
        user.setRole("ADMIN"); // Default role
        return user;
    }
}
