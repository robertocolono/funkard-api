package com.funkard.admin.repository;

import com.funkard.admin.model.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, UUID> {
    List<SupportMessage> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}
