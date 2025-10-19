package com.funkard.admin.log;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "admin_actions_log")
public class AdminActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // riferimento all'entità su cui è stata fatta l'azione
    private Long targetId;

    // es. "NOTIFICATION", "PRICE", "REPORT"
    private String targetType;

    // es. "APPROVED", "ARCHIVED", "READ", "COMMENTED"
    private String action;

    private String performedBy; // admin o sistema
    private String role;        // ADMIN, STAFF, SYSTEM

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String notes;
}
