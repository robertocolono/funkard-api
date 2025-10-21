package com.funkard.admin.controller;

import com.funkard.admin.service.RolePermissionService;
import com.funkard.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 🔐 Controller per gestione ruoli e permessi
 */
@RestController
@RequestMapping("/api/admin/roles")
@CrossOrigin(origins = {"https://funkard-admin.vercel.app", "http://localhost:3000"})
public class RolePermissionController {

    @Autowired
    private RolePermissionService roleService;

    @Value("${admin.token}")
    private String adminToken;

    /**
     * 📊 Ottieni permessi per un utente
     * GET /api/admin/roles/permissions/{userEmail}
     */
    @GetMapping("/permissions/{userEmail}")
    public ResponseEntity<?> getUserPermissions(
            @PathVariable String userEmail,
            @RequestHeader("X-Admin-Token") String token) {
        
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
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
     */
    @PostMapping("/check-permissions")
    public ResponseEntity<?> checkTicketPermissions(
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-Admin-Token") String token) {
        
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRoles(@RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
