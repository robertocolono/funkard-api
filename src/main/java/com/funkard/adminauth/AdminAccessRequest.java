package com.funkard.adminauth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 📝 Entity per richieste di accesso admin (versione semplificata)
 */
@Entity
@Table(name = "admin_access_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "requested_role", nullable = false, length = 50)
    private String requestedRole;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "approved_by", length = 255)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "related_token", length = 255)
    private String relatedToken;
}

