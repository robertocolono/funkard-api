package com.funkard.admin.controller;

import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.service.AdminNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = {"https://admin.funkard.com", "https://funkard.vercel.app"})
public class AdminNotificationController {

    private final AdminNotificationService service;

    @Value("${admin.token}")
    private String adminToken;

    public AdminNotificationController(AdminNotificationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        if (token == null || !token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        List<AdminNotification> list = service.getActive();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/resolve/{id}")
    public ResponseEntity<?> markResolved(@PathVariable String id,
                                          @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        if (token == null || !token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        service.markResolved(id);
        return ResponseEntity.ok(Map.of("status", "resolved"));
    }
}
