package com.funkard.admin.service;

import com.funkard.admin.dto.SupportStatsDTO;
import com.funkard.admin.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class AdminSupportService {

    private final SupportTicketRepository repo;

    public AdminSupportService(SupportTicketRepository repo) {
        this.repo = repo;
    }

    public List<SupportStatsDTO> getStatsLast30Days() {
        LocalDate today = LocalDate.now();
        List<SupportStatsDTO> data = new ArrayList<>();

        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long opened = repo.countByStatusAndCreatedAtBetween("open", date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            long closed = repo.countByStatusAndUpdatedAtBetween("closed", date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            data.add(new SupportStatsDTO(date, opened, closed));
        }
        return data;
    }
}
