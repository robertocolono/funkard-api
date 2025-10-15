package com.funkard.admin.controller;

import com.funkard.admin.dto.AdminStatsDTO;
import com.funkard.admin.service.AdminStatsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin(origins = {"https://admin.funkard.com", "https://funkard.vercel.app"})
public class AdminStatsController {

    private final AdminStatsService service;

    @Value("${admin.token}")
    private String adminToken;

    public AdminStatsController(AdminStatsService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getStats(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        if (token == null || !token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }

        AdminStatsDTO stats = service.getStats();
        return ResponseEntity.ok(stats);
    }
}
