package com.funkard.repository;

import com.funkard.model.FranchiseProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 📝 Repository per proposte franchise
 */
@Repository
public interface FranchiseProposalRepository extends JpaRepository<FranchiseProposal, Long> {
    
    /**
     * Trova tutte le proposte per stato
     */
    List<FranchiseProposal> findByStatusOrderByCreatedAtDesc(FranchiseProposal.ProposalStatus status);
    
    /**
     * Trova proposte pending
     */
    List<FranchiseProposal> findByStatusOrderByCreatedAtDesc(FranchiseProposal.ProposalStatus status);
    
    /**
     * Trova proposte per utente
     */
    List<FranchiseProposal> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Trova proposte per email utente
     */
    List<FranchiseProposal> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    
    /**
     * Verifica se esiste proposta identica pending
     */
    Optional<FranchiseProposal> findByCategoryAndFranchiseIgnoreCaseAndStatus(
        String category, 
        String franchise, 
        FranchiseProposal.ProposalStatus status
    );
    
    /**
     * Conta proposte pending
     */
    long countByStatus(FranchiseProposal.ProposalStatus status);
}

