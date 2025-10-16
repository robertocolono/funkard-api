package com.funkard.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardDTO {
    // Statistiche generali
    public long totalUsers;
    public long totalCards;
    public long totalSales;
    public long totalRevenue;
    
    // Statistiche notifiche
    public long unreadNotifications;
    public long urgentNotifications;
    
    // Statistiche supporto
    public long openTickets;
    public long urgentTickets;
    public long resolvedTicketsToday;
    
    // Statistiche valutazioni
    public long pendingValuations;
    public long newValuationsToday;
    
    // Notifiche recenti
    public List<NotificationDTO> recentNotifications;
    
    // Ticket recenti
    public List<SupportTicketDTO> recentTickets;
    
    // Timestamp
    public LocalDateTime lastUpdated;
    
    public DashboardDTO() {
        this.lastUpdated = LocalDateTime.now();
    }
}
