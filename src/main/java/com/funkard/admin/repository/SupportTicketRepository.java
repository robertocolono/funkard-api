package com.funkard.admin.repository;

import com.funkard.admin.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
    
    // Metodi per statistiche support tickets
    long countByStatus(String status);
    long countByStatusAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);
    long countByStatusAndUpdatedAtBetween(String status, LocalDateTime start, LocalDateTime end);
    
    // Metodi per gestione nuovi messaggi
    long countByHasNewMessagesTrue();
    
    // Metodo per trovare ticket risolti o chiusi più vecchi di un certo timestamp
    @Query("SELECT t FROM SupportTicket t WHERE (t.status = 'resolved' OR t.status = 'closed') AND t.resolvedAt IS NOT NULL AND t.resolvedAt < :cutoff")
    List<SupportTicket> findResolvedOrClosedOlderThan(@Param("cutoff") LocalDateTime cutoff);
}