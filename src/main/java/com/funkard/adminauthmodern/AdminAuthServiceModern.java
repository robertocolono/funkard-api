package com.funkard.adminauthmodern;

import com.funkard.adminauth.AdminToken;
import com.funkard.adminauth.AdminTokenRepository;
import com.funkard.adminauth.AdminUser;
import com.funkard.adminauth.AdminUserRepository;
import com.funkard.adminauth.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔐 Service moderno per autenticazione admin
 * Usa BCrypt per password hashing
 * Usa AdminSessionServiceModern per gestione sessioni
 */
@Service
public class AdminAuthServiceModern {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthServiceModern.class);
    
    private final AdminUserRepository adminUserRepository;
    private final AdminTokenRepository adminTokenRepository;
    private final AdminSessionServiceModern sessionService;
    private final AdminUserService adminUserService;
    private final PasswordEncoder passwordEncoder;
    
    public AdminAuthServiceModern(
            AdminUserRepository adminUserRepository,
            AdminTokenRepository adminTokenRepository,
            AdminSessionServiceModern sessionService,
            AdminUserService adminUserService) {
        this.adminUserRepository = adminUserRepository;
        this.adminTokenRepository = adminTokenRepository;
        this.sessionService = sessionService;
        this.adminUserService = adminUserService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    /**
     * 🔐 Login admin con email e password
     * @param email Email dell'admin
     * @param password Password in plain text
     * @return Map con sessionId e admin data se successo
     * @throws IllegalArgumentException se credenziali non valide
     */
    @Transactional
    public Map<String, Object> login(String email, String password) {
        logger.warn("LOGIN DEBUG — email ricevuta: {}", email);
        logger.warn("LOGIN DEBUG — password ricevuta: {}", password != null ? "[PRESENTE]" : "[NULL]");
        
        if (email == null || email.trim().isEmpty()) {
            logger.warn("LOGIN DEBUG — email vuota o null");
            throw new IllegalArgumentException("Email richiesta");
        }
        
        if (password == null || password.trim().isEmpty()) {
            logger.warn("LOGIN DEBUG — password vuota o null");
            throw new IllegalArgumentException("Password richiesta");
        }
        
        // Trova admin per email
        Optional<AdminUser> adminOpt = adminUserRepository.findByEmail(email);
        logger.warn("LOGIN DEBUG — utente trovato: {}", adminOpt.isPresent());
        
        if (adminOpt.isEmpty()) {
            logger.warn("⚠️ Tentativo login con email non trovata: {}", email);
            throw new IllegalArgumentException("Credenziali non valide");
        }
        
        AdminUser admin = adminOpt.get();
        logger.warn("LOGIN DEBUG — admin.id: {}", admin.getId());
        logger.warn("LOGIN DEBUG — admin.active: {}", admin.isActive());
        logger.warn("LOGIN DEBUG — admin.onboardingCompleted: {}", admin.isOnboardingCompleted());
        logger.warn("LOGIN DEBUG — admin.passwordHash presente: {}", admin.getPasswordHash() != null && !admin.getPasswordHash().trim().isEmpty());
        if (admin.getPasswordHash() != null) {
            logger.warn("LOGIN DEBUG — admin.passwordHash formato: {}", 
                admin.getPasswordHash().length() > 7 ? admin.getPasswordHash().substring(0, 7) : "TOO_SHORT");
        }
        
        // Verifica che admin sia attivo e onboarding completato
        if (!admin.isActive()) {
            logger.warn("⚠️ Tentativo login con admin non attivo: {}", email);
            throw new IllegalArgumentException("Account non attivo");
        }
        
        if (!admin.isOnboardingCompleted()) {
            logger.warn("⚠️ Tentativo login con onboarding non completato: {}", email);
            throw new IllegalArgumentException("Onboarding non completato");
        }
        
        // Verifica password
        if (admin.getPasswordHash() == null || admin.getPasswordHash().trim().isEmpty()) {
            logger.warn("⚠️ Admin senza password hash: {}", email);
            throw new IllegalArgumentException("Credenziali non valide");
        }
        
        boolean passwordMatches = passwordEncoder.matches(password, admin.getPasswordHash());
        logger.warn("LOGIN DEBUG — password combacia: {}", passwordMatches);
        
        if (!passwordMatches) {
            logger.warn("⚠️ Password non valida per: {}", email);
            throw new IllegalArgumentException("Credenziali non valide");
        }
        
        // Aggiorna lastLoginAt
        admin.setLastLoginAt(LocalDateTime.now());
        adminUserRepository.save(admin);
        
        // Crea sessione
        String sessionId = sessionService.createSession(admin.getId());
        logger.warn("LOGIN DEBUG — sessionId creato: {}", sessionId != null ? sessionId.substring(0, Math.min(8, sessionId.length())) + "..." : "NULL");
        
        logger.info("✅ Login admin moderno riuscito: {} ({})", 
            admin.getDisplayName() != null ? admin.getDisplayName() : admin.getName(), 
            email);
        
        logger.warn("LOGIN DEBUG — ritorno success=true");
        
        return Map.of(
            "success", true,
            "sessionId", sessionId,
            "admin", Map.of(
                "id", admin.getId().toString(),
                "email", admin.getEmail(),
                "role", admin.getRole(),
                "displayName", admin.getDisplayName() != null ? admin.getDisplayName() : admin.getName()
            )
        );
    }
    
    /**
     * ✅ Completa onboarding di un admin usando token di ruolo (AdminToken)
     * @param token Token di onboarding (AdminToken)
     * @param email Email dell'admin
     * @param password Password in plain text (verrà hashata)
     * @param displayName Nome visualizzato
     * @return Map con admin data se successo
     * @throws ResponseStatusException con status code appropriato
     */
    @Transactional
    public Map<String, Object> completeOnboarding(String token, String email, String password, String displayName) {
        // Validazione input
        if (token == null || token.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token richiesto");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email richiesta");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password richiesta");
        }
        
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Display name richiesto");
        }
        
        // Validazione formato email base
        if (!email.contains("@") || email.length() < 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato email non valido");
        }
        
        // Validazione formato password (minimo 8 caratteri)
        if (password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password deve contenere almeno 8 caratteri");
        }
        
        // 🔍 Valida token (AdminToken)
        Optional<AdminToken> tokenOpt = adminTokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            // 🔄 Fallback: prova con token legacy (AdminUser.accessToken)
            logger.info("🔄 Token non trovato in AdminTokenRepository, tentativo fallback legacy per token: {}", token.substring(0, Math.min(8, token.length())) + "...");
            
            try {
                AdminUser savedAdmin = adminUserService.completeOnboarding(token, email, password, displayName);
                
                logger.info("✅ Onboarding legacy completato: {} ({}) con ruolo {}", 
                    displayName, email, savedAdmin.getRole());
                
                // Costruisci la stessa struttura di risposta del flusso moderno
                return Map.of(
                    "success", true,
                    "admin", Map.of(
                        "id", savedAdmin.getId().toString(),
                        "email", savedAdmin.getEmail(),
                        "role", savedAdmin.getRole(),
                        "displayName", savedAdmin.getDisplayName() != null ? savedAdmin.getDisplayName() : savedAdmin.getName()
                    )
                );
                
            } catch (IllegalArgumentException e) {
                String errorMessage = e.getMessage();
                logger.warn("⚠️ Errore durante onboarding legacy: {}", errorMessage);
                
                // Mappa IllegalArgumentException su ResponseStatusException con status appropriato
                HttpStatus status;
                if (errorMessage != null && (
                    errorMessage.contains("già utilizzato") || 
                    errorMessage.contains("onboarding") && errorMessage.contains("già") ||
                    errorMessage.contains("già usato")
                )) {
                    status = HttpStatus.GONE; // 410
                } else if (errorMessage != null && errorMessage.contains("Email già utilizzata")) {
                    status = HttpStatus.CONFLICT; // 409
                } else {
                    status = HttpStatus.BAD_REQUEST; // 400
                }
                
                throw new ResponseStatusException(status, errorMessage);
            }
        }
        
        AdminToken adminToken = tokenOpt.get();
        
        // Verifica che token sia attivo
        if (!adminToken.isActive()) {
            logger.warn("⚠️ Tentativo onboarding con token già usato: {}", adminToken.getId());
            throw new ResponseStatusException(HttpStatus.GONE, "Token già utilizzato");
        }
        
        // Verifica che token non sia scaduto
        if (adminToken.getExpiresAt() != null && adminToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.warn("⚠️ Tentativo onboarding con token scaduto: {}", adminToken.getId());
            throw new ResponseStatusException(HttpStatus.GONE, "Token scaduto");
        }
        
        // 🔍 Verifica se email è già registrata
        Optional<AdminUser> existingAdminOpt = adminUserRepository.findByEmail(email);
        if (existingAdminOpt.isPresent()) {
            logger.warn("⚠️ Tentativo onboarding con email già registrata: {}", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email già registrata");
        }
        
        // 🔑 Recupera ruolo dal token
        String role = adminToken.getRole();
        if (role == null || role.trim().isEmpty()) {
            logger.error("❌ Token senza ruolo: {}", adminToken.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token non valido: ruolo mancante");
        }
        
        // Hash password
        String passwordHash = passwordEncoder.encode(password);
        
        // ➕ Crea nuovo admin
        AdminUser newAdmin = new AdminUser();
        newAdmin.setName(displayName);
        newAdmin.setEmail(email);
        newAdmin.setRole(role);
        newAdmin.setPasswordHash(passwordHash);
        newAdmin.setDisplayName(displayName);
        newAdmin.setOnboardingCompleted(true);
        newAdmin.setOnboardingCompletedAt(LocalDateTime.now());
        newAdmin.setActive(true);
        newAdmin.setAccessToken(null); // Nessun token legacy
        
        AdminUser savedAdmin = adminUserRepository.save(newAdmin);
        
        // 🚫 Invalida token dopo l'uso (monouso)
        adminToken.setActive(false);
        adminTokenRepository.save(adminToken);
        
        logger.info("✅ Onboarding moderno completato: {} ({}) con ruolo {}", 
            displayName, email, role);
        
        return Map.of(
            "success", true,
            "admin", Map.of(
                "id", savedAdmin.getId().toString(),
                "email", savedAdmin.getEmail(),
                "role", savedAdmin.getRole(),
                "displayName", savedAdmin.getDisplayName()
            )
        );
    }
    
    /**
     * 👤 Recupera admin corrente dalla sessione
     * @param sessionId ID della sessione
     * @return Map con admin data se sessione valida
     * @throws IllegalArgumentException se sessione non valida
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentAdmin(String sessionId) {
        logger.warn("🔍 [GET_CURRENT_ADMIN] INIZIO recupero admin corrente");
        logger.warn("  - sessionId ricevuto: {}", sessionId != null ? (sessionId.length() > 8 ? sessionId.substring(0, 8) + "..." : sessionId) : "NULL");
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.warn("❌ [GET_CURRENT_ADMIN] THROW IllegalArgumentException: sessionId null o vuoto");
            throw new IllegalArgumentException("Sessione non valida");
        }
        
        logger.warn("  - Chiamando validateSession()...");
        Optional<UUID> adminIdOpt = sessionService.validateSession(sessionId);
        
        logger.warn("  - validateSession() ritornato: {}", adminIdOpt.isPresent() ? "PRESENT" : "EMPTY");
        
        if (adminIdOpt.isEmpty()) {
            logger.warn("❌ [GET_CURRENT_ADMIN] THROW IllegalArgumentException: validateSession() ritornato empty");
            logger.warn("    - Motivo: sessione non valida o scaduta (verificare log validateSession)");
            throw new IllegalArgumentException("Sessione non valida o scaduta");
        }
        
        UUID adminId = adminIdOpt.get();
        logger.warn("  - ✅ adminId ottenuto da validateSession: {}", adminId);
        logger.warn("  - Cercando admin nel database...");
        
        Optional<AdminUser> adminOpt = adminUserRepository.findById(adminId);
        
        logger.warn("  - Admin trovato nel DB?: {}", adminOpt.isPresent());
        
        if (adminOpt.isEmpty()) {
            logger.warn("❌ [GET_CURRENT_ADMIN] THROW IllegalArgumentException: admin non trovato");
            logger.warn("    - adminId cercato: {}", adminId);
            throw new IllegalArgumentException("Admin non trovato");
        }
        
        AdminUser admin = adminOpt.get();
        logger.warn("  - ✅ Admin trovato:");
        logger.warn("    - admin.id: {}", admin.getId());
        logger.warn("    - admin.email: {}", admin.getEmail());
        logger.warn("    - admin.role: {}", admin.getRole());
        logger.warn("    - admin.active: {}", admin.isActive());
        logger.warn("    - admin.onboardingCompleted: {}", admin.isOnboardingCompleted());
        logger.warn("    - admin.displayName: {}", admin.getDisplayName());
        logger.warn("    - admin.name: {}", admin.getName());
        
        // Verifica che admin sia attivo
        logger.warn("  - Verificando admin.isActive()...");
        if (!admin.isActive()) {
            logger.warn("❌ [GET_CURRENT_ADMIN] THROW IllegalArgumentException: admin non attivo");
            logger.warn("    - admin.id: {}", admin.getId());
            logger.warn("    - admin.email: {}", admin.getEmail());
            logger.warn("    - admin.active: {}", admin.isActive());
            throw new IllegalArgumentException("Account non attivo");
        }
        
        logger.warn("✅ [GET_CURRENT_ADMIN] RETURN SUCCESS: admin valido e attivo");
        logger.warn("    - admin.id: {}", admin.getId());
        logger.warn("    - admin.email: {}", admin.getEmail());
        logger.warn("    - admin.role: {}", admin.getRole());
        
        return Map.of(
            "id", admin.getId().toString(),
            "email", admin.getEmail(),
            "role", admin.getRole(),
            "displayName", admin.getDisplayName() != null ? admin.getDisplayName() : admin.getName()
        );
    }
    
    /**
     * 🚪 Logout admin (invalida sessione)
     * @param sessionId ID della sessione
     */
    @Transactional
    public void logout(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            sessionService.invalidateSession(sessionId);
            logger.info("🚪 Logout admin moderno: sessione invalidata");
        }
    }
}

