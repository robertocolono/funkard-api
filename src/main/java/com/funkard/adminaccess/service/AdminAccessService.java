package com.funkard.adminaccess.service;

import com.funkard.adminaccess.model.AdminAccessRequest;
import com.funkard.adminaccess.model.AdminAccessToken;
import com.funkard.adminaccess.repository.AdminAccessRequestRepository;
import com.funkard.adminaccess.repository.AdminAccessTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔧 Service per gestione token e richieste di accesso admin
 */
@Service
public class AdminAccessService {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccessService.class);
    
    private final AdminAccessTokenRepository tokenRepository;
    private final AdminAccessRequestRepository requestRepository;

    public AdminAccessService(AdminAccessTokenRepository tokenRepository, 
                              AdminAccessRequestRepository requestRepository) {
        this.tokenRepository = tokenRepository;
        this.requestRepository = requestRepository;
        logger.info("✅ AdminAccess module loaded");
    }

    /**
     * ➕ Genera un nuovo token per un ruolo
     * @param role Ruolo (ADMIN, SUPERVISOR, SUPER_ADMIN)
     * @param createdBy Chi crea il token
     * @return Token generato
     */
    @Transactional
    public String generateToken(String role, String createdBy) {
        // Genera token UUID senza trattini
        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        
        AdminAccessToken token = AdminAccessToken.builder()
                .role(role)
                .token(tokenValue)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        tokenRepository.save(token);
        
        logger.info("Token generato per ruolo {} da {}", role, createdBy);
        
        return tokenValue;
    }

    /**
     * 🔍 Valida un token
     * @param token Token da validare
     * @return Token se valido e attivo, altrimenti Optional.empty()
     */
    public Optional<AdminAccessToken> validateToken(String token) {
        Optional<AdminAccessToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }
        
        AdminAccessToken adminToken = tokenOpt.get();
        
        if (!adminToken.isActive()) {
            return Optional.empty();
        }
        
        return Optional.of(adminToken);
    }

    /**
     * 📝 Invia una richiesta di accesso
     * @param email Email dell'utente
     * @param token Token utilizzato
     * @return Richiesta creata
     */
    @Transactional
    public AdminAccessRequest submitRequest(String email, String token) {
        // Valida il token
        Optional<AdminAccessToken> tokenOpt = validateToken(token);
        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Token non valido o non attivo");
        }

        AdminAccessToken adminToken = tokenOpt.get();
        
        // Verifica se esiste già una richiesta pending per questa email e token
        Optional<AdminAccessRequest> existingRequest = requestRepository.findAll().stream()
                .filter(r -> r.getEmail().equals(email) && 
                            r.getRelatedToken() != null && 
                            r.getRelatedToken().equals(token) && 
                            "PENDING".equals(r.getStatus()))
                .findFirst();
        
        if (existingRequest.isPresent()) {
            throw new IllegalArgumentException("Richiesta già esistente per questo token");
        }

        // Crea nuova richiesta
        AdminAccessRequest request = AdminAccessRequest.builder()
                .email(email)
                .requestedRole(adminToken.getRole())
                .relatedToken(token)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        AdminAccessRequest saved = requestRepository.save(request);
        
        logger.info("Richiesta di accesso creata per {} (ruolo: {})", email, adminToken.getRole());
        
        return saved;
    }

    /**
     * 📋 Lista tutte le richieste
     */
    public List<AdminAccessRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    /**
     * ✅ Approva una richiesta
     * @param id ID della richiesta
     * @param approver Chi approva
     * @return Richiesta approvata
     */
    @Transactional
    public AdminAccessRequest approveRequest(UUID id, String approver) {
        AdminAccessRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Richiesta non trovata: " + id));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("Richiesta non in stato PENDING");
        }

        request.setStatus("APPROVED");
        request.setApprovedBy(approver);
        request.setApprovedAt(LocalDateTime.now());
        
        AdminAccessRequest saved = requestRepository.save(request);
        
        logger.info("Richiesta {} approvata da {}", id, approver);
        
        return saved;
    }

    /**
     * ❌ Rifiuta una richiesta
     * @param id ID della richiesta
     * @return Richiesta rifiutata
     */
    @Transactional
    public AdminAccessRequest rejectRequest(UUID id) {
        AdminAccessRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Richiesta non trovata: " + id));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("Richiesta non in stato PENDING");
        }

        request.setStatus("REJECTED");
        
        AdminAccessRequest saved = requestRepository.save(request);
        
        logger.info("Richiesta {} rifiutata", id);
        
        return saved;
    }

    /**
     * 📋 Lista tutti i token attivi
     */
    public List<AdminAccessToken> listTokens() {
        return tokenRepository.findByActiveTrue();
    }
}

