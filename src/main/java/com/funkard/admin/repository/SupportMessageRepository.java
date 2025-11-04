package com.funkard.admin.repository;

import com.funkard.admin.model.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, UUID> {
    List<SupportMessage> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
    
    // Metodo per cancellare messaggi associati a un ticket
    @Modifying
    @Transactional
    @Query("DELETE FROM SupportMessage m WHERE m.ticket.id = :ticketId")
    void deleteByTicketId(@Param("ticketId") UUID ticketId);
}
