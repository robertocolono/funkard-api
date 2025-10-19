package com.funkard.admin.repository;

import com.funkard.admin.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
}