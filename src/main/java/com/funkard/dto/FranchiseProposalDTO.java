package com.funkard.dto;

import com.funkard.model.FranchiseProposal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 📝 DTO per proposta franchise
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FranchiseProposalDTO {
    private Long id;
    private String category;
    private String franchise;
    private String userEmail;
    private Long userId;
    private FranchiseProposal.ProposalStatus status;
    private Long processedById;
    private String processedByEmail;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    
    /**
     * Costruttore da entità FranchiseProposal
     */
    public FranchiseProposalDTO(FranchiseProposal proposal) {
        this.id = proposal.getId();
        this.category = proposal.getCategory();
        this.franchise = proposal.getFranchise();
        this.userEmail = proposal.getUserEmail();
        this.userId = proposal.getUser() != null ? proposal.getUser().getId() : null;
        this.status = proposal.getStatus();
        this.processedById = proposal.getProcessedBy() != null ? proposal.getProcessedBy().getId() : null;
        this.processedByEmail = proposal.getProcessedBy() != null ? proposal.getProcessedBy().getEmail() : null;
        this.processedAt = proposal.getProcessedAt();
        this.createdAt = proposal.getCreatedAt();
    }
}

