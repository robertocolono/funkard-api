package com.funkard.admin.service;

import com.funkard.admin.dto.AdminDashboardDTO;
import com.funkard.admin.repository.AdminNotificationRepository;
import com.funkard.admin.model.AdminNotification;
import com.funkard.grading.repository.GradingRepository;
import com.funkard.grading.model.GradingRequest;
import com.funkard.market.repository.ProductRepository;
import com.funkard.market.model.Product;
import com.funkard.repository.UserRepository;
import com.funkard.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardService {

    private final AdminNotificationRepository notificationRepo;
    private final GradingRepository gradingRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    public AdminDashboardService(
            AdminNotificationRepository notificationRepo,
            GradingRepository gradingRepo,
            ProductRepository productRepo,
            UserRepository userRepo) {
        this.notificationRepo = notificationRepo;
        this.gradingRepo = gradingRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    public AdminDashboardDTO getDashboardStats() {
        return AdminDashboardDTO.builder()
                .notifications(getNotificationStats())
                .market(getMarketStats())
                .grading(getGradingStats())
                .users(getUserStats())
                .support(getSupportStats())
                .marketTrend(getMarketTrend())
                .build();
    }

    private AdminDashboardDTO.NotificationStats getNotificationStats() {
        List<AdminNotification> active = notificationRepo.findByResolvedFalseOrderByCreatedAtDesc();
        List<AdminNotification> resolved = notificationRepo.findByResolvedTrueOrderByCreatedAtDesc();
        
        long critical = active.stream()
                .filter(n -> n.getSeverity() == AdminNotification.Severity.CRITICAL)
                .count();

        return AdminDashboardDTO.NotificationStats.builder()
                .active(active.size())
                .resolved(resolved.size())
                .critical((int) critical)
                .build();
    }

    private AdminDashboardDTO.MarketStats getMarketStats() {
        List<Product> allProducts = productRepo.findAll();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        
        long newThisWeek = allProducts.stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(oneWeekAgo))
                .count();

        // Calcolo cambio valore medio (esempio semplificato)
        double avgValueChange = allProducts.stream()
                .filter(p -> p.getPrice() != null && p.getEstimatedValue() != null)
                .mapToDouble(p -> {
                    double ratio = p.getPrice() / p.getEstimatedValue();
                    return (ratio - 1.0) * 100; // Percentuale di cambio
                })
                .average()
                .orElse(0.0);

        return AdminDashboardDTO.MarketStats.builder()
                .totalProducts(allProducts.size())
                .avgValueChange(Math.round(avgValueChange * 10.0) / 10.0)
                .newThisWeek((int) newThisWeek)
                .build();
    }

    private AdminDashboardDTO.GradingStats getGradingStats() {
        List<GradingRequest> allGradings = gradingRepo.findAll();
        
        long errors = allGradings.stream()
                .filter(g -> "FAILED".equals(g.getStatus()))
                .count();
                
        long inProgress = allGradings.stream()
                .filter(g -> "IN_PROGRESS".equals(g.getStatus()))
                .count();

        return AdminDashboardDTO.GradingStats.builder()
                .total(allGradings.size())
                .errors((int) errors)
                .inProgress((int) inProgress)
                .build();
    }

    private AdminDashboardDTO.UserStats getUserStats() {
        List<User> allUsers = userRepo.findAll();
        
        // Esempio: utenti "flagged" (potresti avere un campo specifico)
        long flagged = allUsers.stream()
                .filter(u -> u.getEmail() != null && u.getEmail().contains("flagged"))
                .count();

        return AdminDashboardDTO.UserStats.builder()
                .total(allUsers.size())
                .flagged((int) flagged)
                .build();
    }

    private AdminDashboardDTO.SupportStats getSupportStats() {
        // Esempio semplificato - potresti avere un SupportTicketRepository
        // Per ora simuliamo con le notifiche di supporto
        List<AdminNotification> supportNotifications = notificationRepo.findAll().stream()
                .filter(n -> n.getType() == AdminNotification.Type.SUPPORT)
                .toList();

        long open = supportNotifications.stream()
                .filter(n -> !n.isResolved())
                .count();
                
        long resolved = supportNotifications.stream()
                .filter(AdminNotification::isResolved)
                .count();

        return AdminDashboardDTO.SupportStats.builder()
                .open((int) open)
                .resolved((int) resolved)
                .build();
    }

    private List<AdminDashboardDTO.MarketTrendPoint> getMarketTrend() {
        // Genera trend degli ultimi 7 giorni
        List<AdminDashboardDTO.MarketTrendPoint> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            String dateStr = date.format(formatter);
            
            // Simula valore di mercato (potresti calcolarlo da dati reali)
            double value = 90 + (Math.random() * 20); // 90-110 range
            
            trend.add(AdminDashboardDTO.MarketTrendPoint.builder()
                    .date(dateStr)
                    .value(Math.round(value * 10.0) / 10.0)
                    .build());
        }
        
        return trend;
    }
}