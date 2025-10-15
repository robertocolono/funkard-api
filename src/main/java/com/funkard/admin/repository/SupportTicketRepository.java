package com.funkard.admin.repository;

import com.funkard.admin.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
    List<SupportTicket> findByUserId(String userId);
    List<SupportTicket> findByStatus(String status);
    long countByStatus(String status);
    long countByStatusAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);
    long countByStatusAndUpdatedAtBetween(String status, LocalDateTime start, LocalDateTime end);
}
