package com.funkard.admin.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_actions_log")
public class AdminActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 a quale entità è riferita l'azione (es. notifica o ticket)
    @Column(name = "target_id")
    private Long targetId;

    // tipo entità: NOTIFICATION, PRICE_REQUEST, REPORT, ecc.
    @Column(name = "target_type")
    private String targetType;

    @Column(name = "action")
    private String action; // es. "APPROVED", "ARCHIVED", "READ", "COMMENTED"

    @Column(name = "performed_by")
    private String performedBy; // admin o sistema

    @Column(name = "role")
    private String role; // ADMIN, STAFF, SYSTEM

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Constructors
    public AdminActionLog() {}

    public AdminActionLog(Long targetId, String targetType, String action, String performedBy, String role, String notes) {
        this.targetId = targetId;
        this.targetType = targetType;
        this.action = action;
        this.performedBy = performedBy;
        this.role = role;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
