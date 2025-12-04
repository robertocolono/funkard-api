package com.funkard.admin.controller;

import com.funkard.admin.dto.AdminDashboardDTO;
import com.funkard.admin.service.AdminDashboardService;
import com.funkard.admin.repository.AdminNotificationRepository;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public AdminDashboardDTO getDashboard() {
        return dashboardService.getDashboard();
    }

    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Map<String, Object> cleanupOldNotifications() {
        int deleted = notificationRepository.deleteByArchivedTrueAndArchivedAtBefore(LocalDateTime.now().minusDays(30));
        return Map.of("deleted", deleted);
    }
}