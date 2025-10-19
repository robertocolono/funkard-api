package com.funkard.admin.repository;

import com.funkard.admin.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
    
    // Metodi per statistiche support tickets
    long countByStatus(String status);
    long countByStatusAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);
    long countByStatusAndUpdatedAtBetween(String status, LocalDateTime start, LocalDateTime end);
}