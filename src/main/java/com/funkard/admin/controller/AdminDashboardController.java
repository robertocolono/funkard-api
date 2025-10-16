package com.funkard.admin.controller;

import com.funkard.admin.dto.AdminDashboardDTO;
import com.funkard.admin.service.AdminDashboardService;
import com.funkard.admin.repository.AdminNotificationRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;
    private final AdminNotificationRepository notificationRepository;

    public AdminDashboardController(AdminDashboardService dashboardService, AdminNotificationRepository notificationRepository) {
        this.dashboardService = dashboardService;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public AdminDashboardDTO getDashboard() {
        return dashboardService.getDashboard();
    }

    @DeleteMapping("/cleanup")
    public Map<String, Object> cleanupOldNotifications() {
        long deleted = notificationRepository.deleteAllResolvedBefore(LocalDateTime.now().minusDays(30));
        return Map.of("deleted", deleted);
    }
}