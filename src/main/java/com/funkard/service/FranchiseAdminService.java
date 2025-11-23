package com.funkard.service;

import com.funkard.model.Franchise;
import com.funkard.model.FranchiseProposal;
import com.funkard.model.User;
import com.funkard.repository.FranchiseRepository;
import com.funkard.repository.FranchiseProposalRepository;
import com.funkard.repository.UserRepository;
import com.funkard.admin.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 📚 Servizio Admin per gestione franchise e proposte
 * 
 * Gestisce approvazioni, rifiuti, abilitazione/disabilitazione
 * e sincronizzazione automatica con franchises.json.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FranchiseAdminService {
    
    private final FranchiseRepository franchiseRepository;
    private final FranchiseProposalRepository proposalRepository;
    private final UserRepository userRepository;
    private final AdminNotificationService adminNotificationService;
    private final FranchiseJsonService franchiseJsonService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${franchise.json.path:data/franchises.json}")
    private String jsonFilePath;
    
    /**
     * 📋 Recupera tutte le proposte e franchise
     * 
     * @param status Filtro opzionale per stato (pending, active, disabled)
     */
    public Map<String, Object> getAllFranchisesAndProposals(String status) {
        Map<String, Object> result = new HashMap<>();
        
        // Proposte
        List<FranchiseProposal> proposals;
        if ("pending".equalsIgnoreCase(status)) {
            proposals = proposalRepository.findByStatusOrderByCreatedAtDesc(
                FranchiseProposal.ProposalStatus.PENDING);
        } else {
            proposals = proposalRepository.findAll();
        }
        
        // Franchise
        List<Franchise> franchises;
        if ("active".equalsIgnoreCase(status)) {
            franchises = franchiseRepository.findByStatusOrderByCategoryAscNameAsc(
                Franchise.FranchiseStatus.ACTIVE);
        } else if ("disabled".equalsIgnoreCase(status)) {
            franchises = franchiseRepository.findByStatusOrderByCategoryAscNameAsc(
                Franchise.FranchiseStatus.DISABLED);
        } else {
            franchises = franchiseRepository.findAll();
        }
        
        result.put("proposals", proposals);
        result.put("franchises", franchises);
        result.put("stats", Map.of(
            "totalProposals", proposalRepository.count(),
            "pendingProposals", proposalRepository.countByStatus(
                FranchiseProposal.ProposalStatus.PENDING),
            "totalFranchises", franchiseRepository.count(),
            "activeFranchises", franchiseRepository.countByStatus(
                Franchise.FranchiseStatus.ACTIVE),
            "disabledFranchises", franchiseRepository.countByStatus(
                Franchise.FranchiseStatus.DISABLED)
        ));
        
        return result;
    }
    
    /**
     * ✅ Approva proposta franchise
     * 
     * 1. Cambia status proposta a APPROVED
     * 2. Crea nuovo record in Franchise
     * 3. Aggiorna franchises.json
     * 4. Invia notifica admin
     */
    @Transactional
    public Franchise approveProposal(Long proposalId, Long adminId) {
        FranchiseProposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposta non trovata"));
        
        if (proposal.getStatus() != FranchiseProposal.ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposta già processata");
        }
        
        // Verifica se franchise esiste già
        if (franchiseRepository.existsByNameIgnoreCase(proposal.getFranchise())) {
            throw new IllegalStateException(
                String.format("Franchise '%s' già esistente", proposal.getFranchise()));
        }
        
        // Recupera admin
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new IllegalArgumentException("Admin non trovato"));
        
        // Crea franchise
        Franchise franchise = new Franchise();
        franchise.setCategory(proposal.getCategory());
        franchise.setName(normalizeName(proposal.getFranchise()));
        franchise.setStatus(Franchise.FranchiseStatus.ACTIVE);
        Franchise savedFranchise = franchiseRepository.save(franchise);
        
        // Aggiorna proposta
        proposal.setStatus(FranchiseProposal.ProposalStatus.APPROVED);
        proposal.setProcessedBy(admin);
        proposal.setProcessedAt(LocalDateTime.now());
        proposalRepository.save(proposal);
        
        // Aggiorna cache JSON
        try {
            franchiseJsonService.updateJsonFile(
                savedFranchise.getCategory(), 
                savedFranchise.getName(), 
                true
            );
            log.info("✅ Cache JSON aggiornata: aggiunto {} - {}", 
                savedFranchise.getCategory(), savedFranchise.getName());
        } catch (Exception e) {
            log.error("❌ Errore durante aggiornamento cache JSON: {}", e.getMessage(), e);
            // Non bloccare l'operazione se JSON fallisce
        }
        
        // Notifica admin
        adminNotificationService.createAdminNotification(
            "Franchise approvato",
            String.format("Franchise '%s' approvato e aggiunto al catalogo (categoria: %s)", 
                savedFranchise.getName(), savedFranchise.getCategory()),
            "normal",
            "franchise_approved"
        );
        
        log.info("✅ Proposta {} approvata da admin {}: {} - {}", 
            proposalId, adminId, savedFranchise.getCategory(), savedFranchise.getName());
        
        return savedFranchise;
    }
    
    /**
     * ❌ Rifiuta proposta franchise
     */
    @Transactional
    public void rejectProposal(Long proposalId, Long adminId) {
        FranchiseProposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposta non trovata"));
        
        if (proposal.getStatus() != FranchiseProposal.ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposta già processata");
        }
        
        // Recupera admin
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new IllegalArgumentException("Admin non trovato"));
        
        // Aggiorna proposta
        proposal.setStatus(FranchiseProposal.ProposalStatus.REJECTED);
        proposal.setProcessedBy(admin);
        proposal.setProcessedAt(LocalDateTime.now());
        proposalRepository.save(proposal);
        
        // Notifica admin
        adminNotificationService.createAdminNotification(
            "Proposta franchise rifiutata",
            String.format("Proposta '%s' (categoria: %s) rifiutata", 
                proposal.getFranchise(), proposal.getCategory()),
            "normal",
            "franchise_rejected"
        );
        
        // TODO: Invia email utente se userEmail presente
        if (proposal.getUserEmail() != null && !proposal.getUserEmail().isEmpty()) {
            log.info("📧 Email rifiuto da inviare a: {}", proposal.getUserEmail());
            // TODO: Implementare invio email
        }
        
        log.info("❌ Proposta {} rifiutata da admin {}: {} - {}", 
            proposalId, adminId, proposal.getCategory(), proposal.getFranchise());
    }
    
    /**
     * 🚫 Disabilita franchise
     * 
     * Rimuove temporaneamente dal JSON pubblico
     */
    @Transactional
    public Franchise disableFranchise(Long franchiseId, Long adminId) {
        Franchise franchise = franchiseRepository.findById(franchiseId)
            .orElseThrow(() -> new IllegalArgumentException("Franchise non trovato"));
        
        if (franchise.getStatus() == Franchise.FranchiseStatus.DISABLED) {
            throw new IllegalStateException("Franchise già disabilitato");
        }
        
        franchise.setStatus(Franchise.FranchiseStatus.DISABLED);
        Franchise updated = franchiseRepository.save(franchise);
        
        // Rimuovi da cache JSON
        try {
            franchiseJsonService.updateJsonFile(
                franchise.getCategory(), 
                franchise.getName(), 
                false
            );
            log.info("✅ Cache JSON aggiornata: rimosso {} - {}", 
                franchise.getCategory(), franchise.getName());
        } catch (Exception e) {
            log.error("❌ Errore durante aggiornamento cache JSON: {}", e.getMessage(), e);
        }
        
        // Notifica admin
        adminNotificationService.createAdminNotification(
            "Franchise disabilitato",
            String.format("Franchise '%s' disabilitato (categoria: %s)", 
                franchise.getName(), franchise.getCategory()),
            "normal",
            "franchise_disabled"
        );
        
        log.info("🚫 Franchise {} disabilitato da admin {}", franchiseId, adminId);
        
        return updated;
    }
    
    /**
     * ✅ Riabilita franchise
     * 
     * Aggiunge di nuovo al JSON pubblico
     */
    @Transactional
    public Franchise enableFranchise(Long franchiseId, Long adminId) {
        Franchise franchise = franchiseRepository.findById(franchiseId)
            .orElseThrow(() -> new IllegalArgumentException("Franchise non trovato"));
        
        if (franchise.getStatus() == Franchise.FranchiseStatus.ACTIVE) {
            throw new IllegalStateException("Franchise già abilitato");
        }
        
        franchise.setStatus(Franchise.FranchiseStatus.ACTIVE);
        Franchise updated = franchiseRepository.save(franchise);
        
        // Aggiungi a cache JSON
        try {
            franchiseJsonService.updateJsonFile(
                franchise.getCategory(), 
                franchise.getName(), 
                true
            );
            log.info("✅ Cache JSON aggiornata: aggiunto {} - {}", 
                franchise.getCategory(), franchise.getName());
        } catch (Exception e) {
            log.error("❌ Errore durante aggiornamento cache JSON: {}", e.getMessage(), e);
        }
        
        // Notifica admin
        adminNotificationService.createAdminNotification(
            "Franchise riabilitato",
            String.format("Franchise '%s' riabilitato (categoria: %s)", 
                franchise.getName(), franchise.getCategory()),
            "normal",
            "franchise_enabled"
        );
        
        log.info("✅ Franchise {} riabilitato da admin {}", franchiseId, adminId);
        
        return updated;
    }
    
    /**
     * ➕ Crea franchise manualmente (admin)
     * 
     * Salva nel DB e aggiorna JSON
     */
    @Transactional
    public Franchise createFranchise(String category, String name, Long adminId) {
        // Verifica duplicati
        if (franchiseRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalStateException(
                String.format("Franchise '%s' già esistente", name));
        }
        
        // Crea franchise
        Franchise franchise = new Franchise();
        franchise.setCategory(category);
        franchise.setName(normalizeName(name));
        franchise.setStatus(Franchise.FranchiseStatus.ACTIVE);
        Franchise saved = franchiseRepository.save(franchise);
        
        // Aggiorna cache JSON
        try {
            franchiseJsonService.updateJsonFile(
                saved.getCategory(), 
                saved.getName(), 
                true
            );
            log.info("✅ Cache JSON aggiornata: aggiunto {} - {}", 
                saved.getCategory(), saved.getName());
        } catch (Exception e) {
            log.error("❌ Errore durante aggiornamento cache JSON: {}", e.getMessage(), e);
        }
        
        // Notifica admin
        adminNotificationService.createAdminNotification(
            "Franchise creato manualmente",
            String.format("Franchise '%s' creato manualmente (categoria: %s)", 
                saved.getName(), saved.getCategory()),
            "normal",
            "franchise_created"
        );
        
        log.info("➕ Franchise creato manualmente da admin {}: {} - {}", 
            adminId, saved.getCategory(), saved.getName());
        
        return saved;
    }
    
    /**
     * 📝 Crea proposta franchise (da utente)
     */
    @Transactional
    public FranchiseProposal createProposal(String category, String franchise, 
                                            String userEmail, Long userId) {
        // Verifica duplicati pending
        Optional<FranchiseProposal> existing = proposalRepository
            .findByCategoryAndFranchiseIgnoreCaseAndStatus(
                category, franchise, FranchiseProposal.ProposalStatus.PENDING);
        
        if (existing.isPresent()) {
            throw new IllegalStateException(
                "Una proposta identica è già in attesa di approvazione");
        }
        
        // Verifica se franchise esiste già
        if (franchiseRepository.existsByNameIgnoreCase(franchise)) {
            throw new IllegalStateException(
                String.format("Franchise '%s' già esistente nel catalogo", franchise));
        }
        
        // Crea proposta
        FranchiseProposal proposal = new FranchiseProposal();
        proposal.setCategory(category);
        proposal.setFranchise(normalizeName(franchise));
        proposal.setUserEmail(userEmail);
        
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            proposal.setUser(user);
        }
        
        proposal.setStatus(FranchiseProposal.ProposalStatus.PENDING);
        FranchiseProposal saved = proposalRepository.save(proposal);
        
        // Notifica admin
        adminNotificationService.createAdminNotification(
            "Nuova proposta franchise",
            String.format("Categoria: %s - Franchise: %s", category, franchise),
            "normal",
            "franchise_proposal"
        );
        
        log.info("📝 Proposta franchise creata: {} - {} (utente: {})", 
            category, franchise, userId != null ? userId : userEmail);
        
        return saved;
    }
    
    
    /**
     * 🔍 Normalizza nome franchise
     */
    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }
        
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        
        // Capitalizza prima lettera
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1);
    }
}

