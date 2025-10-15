package com.funkard.admin;

import com.funkard.admin.dto.PendingItemDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/valuation")
@CrossOrigin(origins = {"https://funkard.vercel.app", "http://localhost:3000"})
public class AdminController {

    private final AdminService service;
    
    @Value("${admin.token}")
    private String adminToken;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingItems(@RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(service.getPendingItems());
    }
    
    @GetMapping("/check")
    public ResponseEntity<?> checkAdmin(@RequestHeader("X-Admin-Token") String headerToken) {
        if (!headerToken.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token");
        }
        return ResponseEntity.ok("Access granted");
    }
}
