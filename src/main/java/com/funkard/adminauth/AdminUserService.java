package com.funkard.adminauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔐 Service per gestione utenti admin
 */
@Service
public class AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);
    
    private final AdminUserRepository repository;

    public AdminUserService(AdminUserRepository repository) {
        this.repository = repository;
    }

    /**
     * 🔍 Trova un utente admin per token (solo se attivo)
     */
    public AdminUser getByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        return repository.findByAccessToken(token)
                .filter(AdminUser::isActive)
                .orElse(null);
    }

    /**
     * ➕ Crea un nuovo utente admin con token generato
     */
    @Transactional
    public AdminUser createUser(String name, String email, String role) {
        // Verifica se l'email esiste già
        if (repository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email già esistente: " + email);
        }

        // Genera token univoco (128 caratteri)
        String token = generateToken();

        AdminUser user = new AdminUser(name, email, role, token);
        AdminUser saved = repository.save(user);
        
        logger.info("✅ Creato nuovo utente admin: {} ({})", name, email);
        return saved;
    }

    /**
     * 🔄 Rigenera token per un utente esistente
     */
    @Transactional
    public AdminUser regenerateToken(UUID id) {
        AdminUser user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + id));

        // Genera nuovo token
        String newToken = generateToken();
        user.setAccessToken(newToken);
        
        AdminUser updated = repository.save(user);
        logger.info("🔄 Token rigenerato per utente: {} ({})", user.getName(), user.getEmail());
        
        return updated;
    }

    /**
     * 🚫 Disattiva un utente admin
     */
    @Transactional
    public void deactivate(UUID id) {
        AdminUser user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + id));

        user.setActive(false);
        repository.save(user);
        
        logger.info("🚫 Utente admin disattivato: {} ({})", user.getName(), user.getEmail());
    }

    /**
     * 🎯 Assicura che esista un SUPER_ADMIN
     * Se non esiste, lo crea con token da variabile d'ambiente SUPER_ADMIN_TOKEN
     * Se esiste già, aggiorna l'email a colonoroberto@gmail.com e il token se necessario
     */
    @Transactional
    public void ensureSuperAdminExists() {
        // Leggi token da variabile d'ambiente
        String superAdminToken = System.getenv("SUPER_ADMIN_TOKEN");
        
        if (superAdminToken == null || superAdminToken.trim().isEmpty()) {
            logger.warn("⚠️ SUPER_ADMIN_TOKEN non trovato nelle variabili d'ambiente. Generazione token automatica...");
            // Genera token se non presente
            superAdminToken = generateToken();
        }

        // Verifica se esiste già un SUPER_ADMIN attivo
        Optional<AdminUser> existingSuperAdmin = repository.findFirstByRoleAndActiveTrue("SUPER_ADMIN");
        
        if (existingSuperAdmin.isPresent()) {
            // Aggiorna SUPER_ADMIN esistente
            AdminUser superAdmin = existingSuperAdmin.get();
            boolean updated = false;
            
            // Aggiorna email se diversa
            if (!"colonoroberto@gmail.com".equals(superAdmin.getEmail())) {
                superAdmin.setEmail("colonoroberto@gmail.com");
                updated = true;
                logger.info("📧 Email SUPER_ADMIN aggiornata a: colonoroberto@gmail.com");
            }
            
            // Aggiorna token se diverso
            if (!superAdminToken.equals(superAdmin.getAccessToken())) {
                superAdmin.setAccessToken(superAdminToken);
                updated = true;
                logger.info("🔑 Token SUPER_ADMIN aggiornato da variabile d'ambiente");
            }
            
            // Assicura che name, role e active siano corretti
            if (!"Will".equals(superAdmin.getName())) {
                superAdmin.setName("Will");
                updated = true;
            }
            if (!"SUPER_ADMIN".equals(superAdmin.getRole())) {
                superAdmin.setRole("SUPER_ADMIN");
                updated = true;
            }
            if (!superAdmin.isActive()) {
                superAdmin.setActive(true);
                updated = true;
            }
            
            if (updated) {
                repository.save(superAdmin);
                logger.info("✅ SUPER_ADMIN 'Will' aggiornato con successo (email: colonoroberto@gmail.com)");
            } else {
                logger.info("✅ SUPER_ADMIN 'Will' già presente e aggiornato (email: colonoroberto@gmail.com)");
            }
            return;
        }

        // Crea nuovo SUPER_ADMIN "Will"
        try {
            AdminUser superAdmin = new AdminUser(
                "Will",
                "colonoroberto@gmail.com",
                "SUPER_ADMIN",
                superAdminToken
            );
            
            repository.save(superAdmin);
            String tokenPreview = superAdmin.getAccessToken() != null && superAdmin.getAccessToken().length() > 20 
                ? superAdmin.getAccessToken().substring(0, 20) + "..." 
                : "***";
            logger.info("✅ SUPER_ADMIN 'Will' creato con successo (email: {}, token: {})", 
                       superAdmin.getEmail(), tokenPreview);
            
        } catch (Exception e) {
            logger.error("❌ Errore durante la creazione del SUPER_ADMIN: {}", e.getMessage(), e);
        }
    }

    /**
     * 🔑 Genera un token univoco di 128 caratteri
     * Usa UUID + SHA256 per garantire unicità e lunghezza fissa
     */
    private String generateToken() {
        String uuid = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String combined = uuid + timestamp + UUID.randomUUID().toString();
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            
            // Converti in hex e assicurati che sia esattamente 128 caratteri
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            // SHA-256 produce 64 caratteri hex, dupliciamo per arrivare a 128
            String baseToken = hexString.toString();
            return baseToken + baseToken;
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("Errore durante la generazione del token SHA-256", e);
            // Fallback: usa UUID multipli concatenati
            return (UUID.randomUUID().toString() + UUID.randomUUID().toString() + 
                   UUID.randomUUID().toString() + UUID.randomUUID().toString())
                   .replace("-", "").substring(0, 128);
        }
    }
}

