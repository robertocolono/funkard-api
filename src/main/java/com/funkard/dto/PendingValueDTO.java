package com.funkard.dto;

import com.funkard.model.PendingValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ⏳ DTO per valori personalizzati pending
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingValueDTO {
    private UUID id;
    private PendingValue.ValueType type;
    private String value;
    private Long submittedById;
    private String submittedByEmail;
    private LocalDateTime createdAt;
    private Boolean approved;
    private Long approvedById;
    private String approvedByEmail;
    private LocalDateTime approvedAt;
    
    /**
     * Costruttore da entità PendingValue
     */
    public PendingValueDTO(PendingValue pendingValue) {
        this.id = pendingValue.getId();
        this.type = pendingValue.getType();
        this.value = pendingValue.getValue();
        this.submittedById = pendingValue.getSubmittedBy() != null ? pendingValue.getSubmittedBy().getId() : null;
        this.submittedByEmail = pendingValue.getSubmittedBy() != null ? pendingValue.getSubmittedBy().getEmail() : null;
        this.createdAt = pendingValue.getCreatedAt();
        this.approved = pendingValue.getApproved();
        this.approvedById = pendingValue.getApprovedBy() != null ? pendingValue.getApprovedBy().getId() : null;
        this.approvedByEmail = pendingValue.getApprovedBy() != null ? pendingValue.getApprovedBy().getEmail() : null;
        this.approvedAt = pendingValue.getApprovedAt();
    }
}

