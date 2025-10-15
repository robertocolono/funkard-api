package com.funkard.admin.controller;

import com.funkard.admin.dto.MarketOverviewDTO;
import com.funkard.admin.service.AdminValuationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/valuation")
@CrossOrigin(origins = {"https://admin.funkard.com", "https://funkard.vercel.app"})
public class AdminValuationController {

    private final AdminValuationService service;

    @Value("${admin.token}")
    private String adminToken;

    public AdminValuationController(AdminValuationService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getOverview(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        if (token == null || !token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }

        List<MarketOverviewDTO> overview = service.getOverviewLast7Days();
        return ResponseEntity.ok(overview);
    }
}
