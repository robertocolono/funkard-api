package com.funkard.admin.repository;

import com.funkard.admin.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
    
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(String status);
    
    List<SupportTicket> findByPriorityOrderByCreatedAtDesc(String priority);
    
    List<SupportTicket> findByCategoryOrderByCreatedAtDesc(String category);
    
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
    
    long countByStatus(String status);
    
    long countByPriority(String priority);
    
    long countByCategory(String category);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<SupportTicket> findRecentTickets(LocalDateTime since);
    
    @Query("SELECT t FROM SupportTicket t WHERE t.status IN ('open', 'in_progress') ORDER BY t.createdAt DESC")
    List<SupportTicket> findActiveTickets();
    
    long countByStatusAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);
    
    long countByStatusAndUpdatedAtBetween(String status, LocalDateTime start, LocalDateTime end);
}