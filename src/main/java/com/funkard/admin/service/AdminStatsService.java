package com.funkard.admin.service;

import com.funkard.admin.dto.AdminStatsDTO;
import com.funkard.repository.UserRepository;
import com.funkard.repository.UserCardRepository;
import com.funkard.market.trend.TrendRepository;
import com.funkard.admin.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminStatsService {

    private final UserRepository userRepo;
    private final UserCardRepository cardRepo;
    private final TrendRepository trendRepo;
    private final SupportTicketRepository ticketRepo;

    public AdminStatsService(UserRepository userRepo,
                             UserCardRepository cardRepo,
                             TrendRepository trendRepo,
                             SupportTicketRepository ticketRepo) {
        this.userRepo = userRepo;
        this.cardRepo = cardRepo;
        this.trendRepo = trendRepo;
        this.ticketRepo = ticketRepo;
    }

    public AdminStatsDTO getStats() {
        AdminStatsDTO dto = new AdminStatsDTO();
        dto.users = userRepo.count();
        dto.cards = cardRepo.count();

        // Pending: prodotti con storico insufficiente o manualCheck attivo
        dto.pending = trendRepo.countByManualCheckTrue();

        // Ticket aperti
        dto.tickets = ticketRepo.countByStatus("open");
        return dto;
    }
}
