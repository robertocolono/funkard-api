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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminDashboardService {

    private final AdminNotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final GradingRepository gradingRepository;

    public AdminDashboardService(
        AdminNotificationRepository notificationRepository,
        ProductRepository productRepository,
        UserRepository userRepository,
        GradingRepository gradingRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.gradingRepository = gradingRepository;
    }

    public AdminDashboardDTO getDashboard() {
        AdminDashboardDTO dto = new AdminDashboardDTO();

        // 🔔 NOTIFICHE
        AdminDashboardDTO.NotificationStats notif = new AdminDashboardDTO.NotificationStats();
            notif.setActive(notificationRepository.countByReadFalse());
            notif.setResolved(notificationRepository.count() - notificationRepository.countByReadFalse());
            notif.setCritical(notificationRepository.countByPriority("CRITICAL"));
        dto.setNotifications(notif);

        // 📈 MERCATO
        AdminDashboardDTO.MarketStats market = new AdminDashboardDTO.MarketStats();
        market.setTotalProducts(productRepository.count());
        market.setNewThisWeek(productRepository.countByCreatedAtAfter(LocalDate.now().minusDays(7)));
        market.setAvgValueChange(productRepository.calculateAverageValueChangeLast30Days());
        dto.setMarket(market);

        // 🧾 GRADING
        AdminDashboardDTO.GradingStats grading = new AdminDashboardDTO.GradingStats();
        grading.setTotal(gradingRepository.count());
        grading.setErrors(gradingRepository.countByStatus("FAILED"));
        grading.setInProgress(gradingRepository.countByStatus("IN_PROGRESS"));
        dto.setGrading(grading);

        // 🧍 UTENTI
        AdminDashboardDTO.UserStats users = new AdminDashboardDTO.UserStats();
        users.setTotal(userRepository.count());
        users.setFlagged(userRepository.countByFlaggedTrue());
        dto.setUsers(users);

        // 🆘 SUPPORTO
        AdminDashboardDTO.SupportStats support = new AdminDashboardDTO.SupportStats();
            support.setOpen(notificationRepository.countByType("SUPPORT"));
            support.setResolved(notificationRepository.countByType("SUPPORT"));
        dto.setSupport(support);

        // 📊 TREND MERCATO (mock se non hai storico ancora)
        List<AdminDashboardDTO.MarketTrendPoint> trend = new ArrayList<>();
        for (int i = 30; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            trend.add(new AdminDashboardDTO.MarketTrendPoint(date.toString(), 90 + Math.random() * 10));
        }
        dto.setMarketTrend(trend);

        return dto;
    }
}