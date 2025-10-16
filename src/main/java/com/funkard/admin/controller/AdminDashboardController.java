package com.funkard.admin.controller;

import com.funkard.admin.dto.DashboardDTO;
import com.funkard.admin.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = {"https://admin.funkard.com", "https://funkard.vercel.app"})
public class AdminDashboardController {
    
    private final AdminDashboardService service;
    
    @Value("${admin.token}")
    private String adminToken;
    
    public AdminDashboardController(AdminDashboardService service) {
        this.service = service;
    }
    
    // 🔹 Dashboard aggregata
    @GetMapping
    public ResponseEntity<?> getDashboard(@RequestHeader("Authorization") String authorization) {
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        DashboardDTO dashboard = service.getDashboardData();
        return ResponseEntity.ok(dashboard);
    }
}
