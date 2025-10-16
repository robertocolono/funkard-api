package com.funkard.admin.service;

import com.funkard.admin.dto.DashboardDTO;
import com.funkard.admin.dto.NotificationDTO;
import com.funkard.admin.dto.SupportTicketDTO;
import com.funkard.admin.repository.AdminNotificationRepository;
import com.funkard.admin.repository.SupportTicketRepository;
import com.funkard.repository.UserRepository;
import com.funkard.repository.UserCardRepository;
import com.funkard.market.trend.TrendRepository;
import com.funkard.market.repository.MarketListingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminDashboardService {
    
    private final UserRepository userRepository;
    private final UserCardRepository cardRepository;
    private final MarketListingRepository listingRepository;
    private final TrendRepository trendRepository;
    private final AdminNotificationRepository notificationRepository;
    private final SupportTicketRepository ticketRepository;
    
    public AdminDashboardService(
            UserRepository userRepository,
            UserCardRepository cardRepository,
            MarketListingRepository listingRepository,
            TrendRepository trendRepository,
            AdminNotificationRepository notificationRepository,
            SupportTicketRepository ticketRepository) {
        
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.listingRepository = listingRepository;
        this.trendRepository = trendRepository;
        this.notificationRepository = notificationRepository;
        this.ticketRepository = ticketRepository;
    }
    
    public DashboardDTO getDashboardData() {
        DashboardDTO dashboard = new DashboardDTO();
        
        // Statistiche generali
        dashboard.totalUsers = userRepository.count();
        dashboard.totalCards = cardRepository.count();
        dashboard.totalSales = listingRepository.countBySoldTrue();
        dashboard.totalRevenue = (long) calculateTotalRevenue();
        
        // Statistiche notifiche
        dashboard.unreadNotifications = notificationRepository.countByReadFalse();
        dashboard.urgentNotifications = notificationRepository.countByPriority("urgent");
        
        // Statistiche supporto
        dashboard.openTickets = ticketRepository.countByStatus("open");
        dashboard.urgentTickets = ticketRepository.countByPriority("urgent");
        dashboard.resolvedTicketsToday = countResolvedTicketsToday();
        
        // Statistiche valutazioni
        dashboard.pendingValuations = trendRepository.countByManualCheckTrue();
        dashboard.newValuationsToday = countNewValuationsToday();
        
        // Dati recenti
        dashboard.recentNotifications = getRecentNotifications();
        dashboard.recentTickets = getRecentTickets();
        
        return dashboard;
    }
    
    private double calculateTotalRevenue() {
        // Calcola il ricavo totale dalle vendite
        // Implementazione semplificata - in realtà dovresti fare una query più complessa
        return listingRepository.findAll().stream()
                .filter(listing -> listing.isSold())
                .mapToDouble(listing -> listing.getPriceEUR())
                .sum();
    }
    
    private long countResolvedTicketsToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return ticketRepository.findRecentTickets(startOfDay).stream()
                .filter(ticket -> "resolved".equals(ticket.getStatus()))
                .count();
    }
    
    private long countNewValuationsToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return trendRepository.findByUpdatedAtAfter(startOfDay).size();
    }
    
    private List<NotificationDTO> getRecentNotifications() {
        return notificationRepository.findRecentNotifications(LocalDateTime.now().minusDays(7))
                .stream()
                .map(NotificationDTO::fromEntity)
                .limit(5)
                .toList();
    }
    
    private List<SupportTicketDTO> getRecentTickets() {
        return ticketRepository.findRecentTickets(LocalDateTime.now().minusDays(7))
                .stream()
                .map(SupportTicketDTO::fromEntity)
                .limit(5)
                .toList();
    }
}
