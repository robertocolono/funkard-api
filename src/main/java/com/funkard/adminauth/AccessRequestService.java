package com.funkard.adminauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 📝 Service per gestione richieste di accesso
 */
@Service
public class AccessRequestService {

    private static final Logger logger = LoggerFactory.getLogger(AccessRequestService.class);
    
    private final AccessRequestRepository requestRepository;
    private final AdminTokenRepository tokenRepository;
    private final AdminUserRepository userRepository;
    private final AdminUserService userService;

    public AccessRequestService(AccessRequestRepository requestRepository,
                                AdminTokenRepository tokenRepository,
                                AdminUserRepository userRepository,
                                AdminUserService userService) {
        this.requestRepository = requestRepository;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * 📝 Crea una nuova richiesta di accesso
     * Verifica il ruolo dal token e crea richiesta PENDING
     * @param email Email dell'utente che richiede accesso
     * @param token Token utilizzato
     * @return Richiesta creata
     */
    @Transactional
    public AccessRequest createRequest(String email, String token) {
        // Valida il token
        Optional<AdminToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || !tokenOpt.get().isActive()) {
            throw new IllegalArgumentException("Token non valido o non attivo");
        }

        AdminToken adminToken = tokenOpt.get();
        
        // Verifica se esiste già una richiesta per questo token
        Optional<AccessRequest> existingRequest = requestRepository.findByTokenUsed(token);
        if (existingRequest.isPresent()) {
            AccessRequest req = existingRequest.get();
            if ("PENDING".equals(req.getStatus())) {
                throw new IllegalArgumentException("Richiesta già esistente per questo token");
            }
        }

        // Crea nuova richiesta
        AccessRequest request = new AccessRequest(email, adminToken.getRole(), token);
        AccessRequest saved = requestRepository.save(request);
        
        logger.info("Access request {} dallo user {}", saved.getId(), saved.getEmail());
        
        return saved;
    }

    /**
     * 📋 Lista tutte le richieste pending
     */
    public List<AccessRequest> getPendingRequests() {
        return requestRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    /**
     * ✅ Approva una richiesta di accesso
     * Crea AdminUser con il ruolo richiesto e disattiva il token usato
     * @param id ID della richiesta
     * @param approverId ID dell'utente che approva
     * @return AdminUser creato
     */
    @Transactional
    public AdminUser approveRequest(UUID id, UUID approverId) {
        AccessRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Richiesta non trovata: " + id));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("Richiesta non in stato PENDING");
        }

        // Verifica se l'email esiste già
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email già registrata: " + request.getEmail());
        }

        // Crea AdminUser con il ruolo richiesto
        // Genera un nuovo token per l'utente
        String userToken = userService.generateUserToken();
        
        AdminUser newUser = new AdminUser();
        newUser.setName(request.getEmail().split("@")[0]); // Usa parte prima di @ come nome
        newUser.setEmail(request.getEmail());
        newUser.setRole(request.getRole());
        newUser.setAccessToken(userToken);
        newUser.setActive(true);
        newUser.setPending(false);
        newUser.setApprovedAt(LocalDateTime.now());
        
        AdminUser savedUser = userRepository.save(newUser);

        // Disattiva il token usato
        Optional<AdminToken> usedToken = tokenRepository.findByToken(request.getTokenUsed());
        if (usedToken.isPresent()) {
            AdminToken token = usedToken.get();
            token.setActive(false);
            tokenRepository.save(token);
        }

        // Aggiorna la richiesta
        request.setStatus("APPROVED");
        request.setApprovedBy(approverId);
        requestRepository.save(request);
        
        logger.info("Access request {} approvata da {} - creato AdminUser: {} ({})", 
            request.getId(), approverId, savedUser.getName(), savedUser.getEmail());
        
        return savedUser;
    }

    /**
     * ❌ Rifiuta una richiesta di accesso
     * @param id ID della richiesta
     */
    @Transactional
    public void rejectRequest(UUID id) {
        AccessRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Richiesta non trovata: " + id));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("Richiesta non in stato PENDING");
        }

        request.setStatus("REJECTED");
        requestRepository.save(request);
        
        logger.info("Access request {} rifiutata", request.getId());
    }
}

