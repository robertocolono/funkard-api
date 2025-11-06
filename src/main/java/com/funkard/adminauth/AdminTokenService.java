package com.funkard.adminauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔑 Service per gestione token di ruolo admin
 */
@Service
public class AdminTokenService {

    private static final Logger logger = LoggerFactory.getLogger(AdminTokenService.class);
    
    private final AdminTokenRepository tokenRepository;

    public AdminTokenService(AdminTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * ➕ Crea un nuovo token di ruolo
     * Genera token SHA256(UUID + timestamp)
     * @param role Ruolo (ADMIN, SUPERVISOR, SUPER_ADMIN)
     * @param creatorId ID dell'utente che crea il token
     * @return Token completo (mostrato solo una volta alla creazione)
     */
    @Transactional
    public String createToken(String role, UUID creatorId) {
        // Genera token SHA256(UUID + timestamp)
        String uuid = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String combined = uuid + timestamp;
        
        String tokenHash = generateSHA256(combined);
        
        AdminToken adminToken = new AdminToken(role, tokenHash, creatorId);
        AdminToken saved = tokenRepository.save(adminToken);
        
        logger.info("Token {} creato per ruolo {}", saved.getId(), role);
        logger.info("Token {} creato per ruolo {}", saved.getToken(), saved.getRole());
        
        // Ritorna il token completo solo una volta alla creazione
        return tokenHash;
    }

    /**
     * 📋 Lista tutti i token attivi
     */
    public List<AdminToken> getAllActiveTokens() {
        return tokenRepository.findByActiveTrue();
    }

    /**
     * 🔍 Valida un token
     * @param token Token da validare
     * @return Token se valido e attivo, altrimenti Optional.empty()
     */
    public Optional<AdminToken> validateToken(String token) {
        Optional<AdminToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }
        
        AdminToken adminToken = tokenOpt.get();
        
        // Verifica se è attivo
        if (!adminToken.isActive()) {
            return Optional.empty();
        }
        
        // Verifica se è scaduto
        if (adminToken.getExpiresAt() != null && adminToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }
        
        return Optional.of(adminToken);
    }

    /**
     * 🚫 Disattiva un token
     * @param id ID del token
     */
    @Transactional
    public void deactivateToken(UUID id) {
        AdminToken token = tokenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Token non trovato: " + id));

        token.setActive(false);
        tokenRepository.save(token);
        
        logger.info("Token {} disattivato", token.getId());
    }

    /**
     * 🔄 Rigenera un token (disabilita il vecchio e crea uno nuovo con stesso ruolo)
     * @param id ID del token da rigenerare
     * @param creatorId ID dell'utente che rigenera
     * @return Nuovo token completo
     */
    @Transactional
    public String regenerateToken(UUID id, UUID creatorId) {
        AdminToken oldToken = tokenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Token non trovato: " + id));

        // Disabilita il vecchio token
        oldToken.setActive(false);
        tokenRepository.save(oldToken);

        // Crea nuovo token con stesso ruolo
        String newToken = createToken(oldToken.getRole(), creatorId);
        
        logger.info("Token {} rigenerato (nuovo token creato)", oldToken.getId());
        
        return newToken;
    }

    /**
     * 🔑 Genera hash SHA256 di una stringa
     */
    private String generateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Converti in hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("Errore durante la generazione del token SHA-256", e);
            // Fallback: usa UUID multipli
            return (UUID.randomUUID().toString() + UUID.randomUUID().toString())
                   .replace("-", "");
        }
    }
}
