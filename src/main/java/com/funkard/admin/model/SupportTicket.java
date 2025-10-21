package com.funkard.admin.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "user_email")
    private String userEmail;
    
    @Column(name = "subject")
    private String subject;
    
    @Column(name = "message")
    private String message;
    
    @Column(name = "status")
    private String status = "open"; // "open", "in_progress", "resolved", "closed"
    
    @Column(name = "priority")
    private String priority = "normal"; // "low", "normal", "high", "urgent"
    
    @Column(name = "category")
    private String category; // "technical", "billing", "general", "grading"
    
    @Column(name = "admin_response")
    private String adminResponse;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "has_new_messages")
    private boolean hasNewMessages = false;
    
    @Column(name = "assigned_to")
    private String assignedTo; // email o id del support
    
    // Relazione con User per assegnazione
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private com.funkard.model.User assignedToUser;
    
    @Column(name = "locked")
    private boolean locked = false; // true se preso in carico
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SupportMessage> messages = new ArrayList<>();
    
    // Constructors
    public SupportTicket() {}
    
    public SupportTicket(String userId, String userEmail, String subject, String message, String category) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.subject = subject;
        this.message = message;
        this.category = category;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public boolean isHasNewMessages() { return hasNewMessages; }
    public void setHasNewMessages(boolean hasNewMessages) { this.hasNewMessages = hasNewMessages; }
    
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    
    public com.funkard.model.User getAssignedToUser() { return assignedToUser; }
    public void setAssignedToUser(com.funkard.model.User assignedToUser) { this.assignedToUser = assignedToUser; }
    
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    
    public List<SupportMessage> getMessages() { return messages; }
    public void setMessages(List<SupportMessage> messages) { this.messages = messages; }
}