package com.funkard.adminauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

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
     * @param name Nome utente
     * @param email Email utente
     * @param role Ruolo (SUPER_ADMIN, ADMIN, SUPERVISOR)
     * @param requester Utente che richiede la creazione (per audit)
     */
    @Transactional
    public AdminUser createUser(String name, String email, String role, AdminUser requester) {
        // Verifica se l'email esiste già
        if (repository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email già esistente: " + email);
        }

        // Se si sta creando un SUPER_ADMIN, solo Root può farlo
        if ("SUPER_ADMIN".equals(role)) {
            if (requester == null || !requester.isRoot()) {
                throw new SecurityException("Solo Root Super Admin può creare altri SUPER_ADMIN");
            }
            logger.info("[ROOT ACTION] {} ha creato un nuovo SUPER_ADMIN: {} ({})", 
                requester.getEmail(), name, email);
        }

        // Genera token univoco (128 caratteri)
        String token = generateToken();

        AdminUser user = new AdminUser(name, email, role, token);
        AdminUser saved = repository.save(user);
        
        logger.info("✅ Creato nuovo utente admin: {} ({}) - Richiesto da: {}", 
            name, email, requester != null ? requester.getEmail() : "system");
        return saved;
    }

    /**
     * 🔄 Rigenera token per un utente esistente
     * @param id ID dell'utente
     * @param requester Utente che richiede la rigenerazione (per audit)
     */
    @Transactional
    public AdminUser regenerateToken(UUID id, AdminUser requester) {
        AdminUser user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + id));

        // Se è un SUPER_ADMIN, solo Root può rigenerare il token
        if ("SUPER_ADMIN".equals(user.getRole())) {
            if (requester == null || !requester.isRoot()) {
                throw new SecurityException("Solo Root Super Admin può rigenerare token di SUPER_ADMIN");
            }
            logger.info("[ROOT ACTION] {} ha rigenerato token per SUPER_ADMIN: {} ({})", 
                requester.getEmail(), user.getName(), user.getEmail());
        }

        // Genera nuovo token
        String newToken = generateToken();
        user.setAccessToken(newToken);
        
        AdminUser updated = repository.save(user);
        logger.info("🔄 Token rigenerato per utente: {} ({}) - Richiesto da: {}", 
            user.getName(), user.getEmail(), requester != null ? requester.getEmail() : "system");
        
        return updated;
    }

    /**
     * 🚫 Disattiva un utente admin
     * @param id ID dell'utente
     * @param requester Utente che richiede la disattivazione (per audit)
     */
    @Transactional
    public void deactivate(UUID id, AdminUser requester) {
        AdminUser user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + id));

        // Non permettere di disattivare se stessi
        if (requester != null && requester.getId().equals(id)) {
            throw new SecurityException("Non puoi disattivare il tuo stesso account");
        }

        // Se è un SUPER_ADMIN, solo Root può disattivarlo
        if ("SUPER_ADMIN".equals(user.getRole())) {
            if (requester == null || !requester.isRoot()) {
                throw new SecurityException("Solo Root Super Admin può disattivare SUPER_ADMIN");
            }
            
            // Verifica invariante: deve esistere sempre almeno un SUPER_ADMIN attivo
            long activeSuperAdmins = repository.findAll().stream()
                    .filter(u -> "SUPER_ADMIN".equals(u.getRole()) && u.isActive())
                    .count();
            
            if (activeSuperAdmins <= 1) {
                throw new SecurityException("Impossibile disattivare: deve esistere sempre almeno un SUPER_ADMIN attivo");
            }
            
            logger.info("[ROOT ACTION] {} ha disattivato SUPER_ADMIN: {} ({})", 
                requester.getEmail(), user.getName(), user.getEmail());
        }

        user.setActive(false);
        repository.save(user);
        
        logger.info("🚫 Utente admin disattivato: {} ({}) - Richiesto da: {}", 
            user.getName(), user.getEmail(), requester != null ? requester.getEmail() : "system");
    }
    
    /**
     * ✅ Riattiva un utente admin
     * @param id ID dell'utente
     * @param requester Utente che richiede la riattivazione (per audit)
     */
    @Transactional
    public void activate(UUID id, AdminUser requester) {
        AdminUser user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + id));

        // Se è un SUPER_ADMIN, solo Root può riattivarlo
        if ("SUPER_ADMIN".equals(user.getRole())) {
            if (requester == null || !requester.isRoot()) {
                throw new SecurityException("Solo Root Super Admin può riattivare SUPER_ADMIN");
            }
            logger.info("[ROOT ACTION] {} ha riattivato SUPER_ADMIN: {} ({})", 
                requester.getEmail(), user.getName(), user.getEmail());
        }

        user.setActive(true);
        repository.save(user);
        
        logger.info("✅ Utente admin riattivato: {} ({}) - Richiesto da: {}", 
            user.getName(), user.getEmail(), requester != null ? requester.getEmail() : "system");
    }
    
    /**
     * 🔄 Cambia ruolo di un utente
     * @param id ID dell'utente
     * @param newRole Nuovo ruolo
     * @param requester Utente che richiede il cambio (per audit)
     */
    @Transactional
    public void changeRole(UUID id, String newRole, AdminUser requester) {
        AdminUser user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + id));

        // Non permettere di cambiare il proprio ruolo
        if (requester != null && requester.getId().equals(id)) {
            throw new SecurityException("Non puoi cambiare il tuo stesso ruolo");
        }

        String oldRole = user.getRole();
        
        // Se si sta promuovendo/demotando verso/da SUPER_ADMIN, solo Root può farlo
        if ("SUPER_ADMIN".equals(oldRole) || "SUPER_ADMIN".equals(newRole)) {
            if (requester == null || !requester.isRoot()) {
                throw new SecurityException("Solo Root Super Admin può promuovere/demotere verso/da SUPER_ADMIN");
            }
            
            // Se si sta demotendo un SUPER_ADMIN, verifica invariante
            if ("SUPER_ADMIN".equals(oldRole) && !"SUPER_ADMIN".equals(newRole)) {
                long activeSuperAdmins = repository.findAll().stream()
                        .filter(u -> "SUPER_ADMIN".equals(u.getRole()) && u.isActive())
                        .count();
                
                if (activeSuperAdmins <= 1) {
                    throw new SecurityException("Impossibile demotere: deve esistere sempre almeno un SUPER_ADMIN attivo");
                }
            }
        }

        user.setRole(newRole);
        repository.save(user);
        
        // Logging chiaro per azioni Root
        if (requester != null && requester.isRoot()) {
            if ("SUPER_ADMIN".equals(oldRole) && !"SUPER_ADMIN".equals(newRole)) {
                logger.info("[ROOT ACTION] {} ha retrocesso {} da SUPER_ADMIN a {}", 
                    requester.getEmail(), user.getEmail(), newRole);
            } else if (!"SUPER_ADMIN".equals(oldRole) && "SUPER_ADMIN".equals(newRole)) {
                logger.info("[ROOT ACTION] {} ha promosso {} a SUPER_ADMIN", 
                    requester.getEmail(), user.getEmail());
            } else {
                logger.info("[ROOT ACTION] {} ha cambiato ruolo di {} da {} a {}", 
                    requester.getEmail(), user.getEmail(), oldRole, newRole);
            }
        } else {
            logger.info("🔄 Ruolo cambiato per utente: {} ({}) da {} a {} - Richiesto da: {}", 
                user.getName(), user.getEmail(), oldRole, newRole, requester != null ? requester.getEmail() : "system");
        }
    }
    
    /**
     * 📋 Lista tutti gli utenti admin
     * @param requester Utente che richiede la lista (per filtrare isRoot)
     */
    public List<Map<String, Object>> listAllUsers(AdminUser requester) {
        boolean isRootRequester = requester != null && requester.isRoot();
        
        return repository.findAll().stream()
                .map(user -> {
                    Map<String, Object> userMap = new LinkedHashMap<>();
                    userMap.put("id", user.getId().toString());
                    userMap.put("name", user.getName());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole());
                    userMap.put("active", user.isActive());
                    
                    // Mostra isRoot solo se il chiamante è root
                    if (isRootRequester) {
                        userMap.put("isRoot", user.isRoot());
                    }
                    
                    String tokenPreview = user.getAccessToken() != null && user.getAccessToken().length() >= 12
                        ? user.getAccessToken().substring(0, 12) + "..."
                        : "***";
                    userMap.put("accessToken", tokenPreview);
                    return userMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * 🎯 Assicura che esista un SUPER_ADMIN (overload senza parametri)
     * Chiama il metodo principale senza token specifico
     */
    @Transactional
    public void ensureSuperAdminExists() {
        ensureSuperAdminExists(null);
    }

    /**
     * 🎯 Assicura che esista un SUPER_ADMIN
     * Se non esiste, lo crea con token da variabile d'ambiente SUPER_ADMIN_TOKEN o token fornito
     * Se esiste già, aggiorna solo l'email a colonoroberto@gmail.com (senza rigenerare il token se non specificato)
     * 
     * @param providedToken Token opzionale da usare (se null, legge da env)
     */
    @Transactional
    public void ensureSuperAdminExists(String providedToken) {
        // Usa token fornito, altrimenti leggi da variabile d'ambiente
        String superAdminToken = providedToken;
        if (superAdminToken == null || superAdminToken.trim().isEmpty()) {
            superAdminToken = System.getenv("SUPER_ADMIN_TOKEN");
            // Se anche da env è null, prova da System.getProperty
            if (superAdminToken == null || superAdminToken.trim().isEmpty()) {
                superAdminToken = System.getProperty("SUPER_ADMIN_TOKEN");
            }
        }
        
        if (superAdminToken == null || superAdminToken.trim().isEmpty()) {
            logger.warn("⚠️ SUPER_ADMIN_TOKEN non trovato nelle variabili d'ambiente. Generazione token automatica...");
            // Genera token se non presente
            superAdminToken = generateToken();
        }

        // Verifica se esiste già un SUPER_ADMIN attivo
        Optional<AdminUser> existingSuperAdmin = repository.findFirstByRoleAndActiveTrue("SUPER_ADMIN");
        
        AdminUser superAdmin;
        boolean wasCreated = false;
        
        if (existingSuperAdmin.isPresent()) {
            // Aggiorna SUPER_ADMIN esistente
            superAdmin = existingSuperAdmin.get();
            boolean updated = false;
            
            // Aggiorna email se diversa (senza modificare il token esistente)
            if (!"colonoroberto@gmail.com".equals(superAdmin.getEmail())) {
                logger.info("📧 Email SUPER_ADMIN aggiornata da '{}' a 'colonoroberto@gmail.com'", superAdmin.getEmail());
                superAdmin.setEmail("colonoroberto@gmail.com");
                updated = true;
            }
            
            // NON aggiornare il token se esiste già - mantieni il token esistente
            // Aggiorna token solo se non è impostato o è completamente vuoto
            if (superAdmin.getAccessToken() == null || superAdmin.getAccessToken().trim().isEmpty()) {
                superAdmin.setAccessToken(superAdminToken);
                updated = true;
                logger.info("🔑 Token SUPER_ADMIN impostato da variabile d'ambiente (token era vuoto)");
            }
            // Altrimenti mantieni il token esistente senza modificarlo
            
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
                logger.info("✅ SUPER_ADMIN 'Will' aggiornato con successo");
            }
        } else {
            // Crea nuovo SUPER_ADMIN "Will" con isRoot=true
            try {
                superAdmin = new AdminUser(
                    "Will",
                    "colonoroberto@gmail.com",
                    "SUPER_ADMIN",
                    superAdminToken,
                    true  // isRoot=true per il Super Admin bootstrappato
                );
                
                repository.save(superAdmin);
                logger.info("✅ SUPER_ADMIN 'Will' creato con successo (isRoot=true)");
                wasCreated = true;
                
            } catch (Exception e) {
                logger.error("❌ Errore durante la creazione del SUPER_ADMIN: {}", e.getMessage(), e);
                return; // Esci se non è stato creato
            }
        }
        
        // Assicura che il Super Admin bootstrappato abbia isRoot=true
        if (!superAdmin.isRoot()) {
            superAdmin.setRoot(true);
            repository.save(superAdmin);
            logger.info("✅ SUPER_ADMIN 'Will' impostato come Root");
        }
        
        // Log finale bootstrap con tutte le informazioni richieste
        logger.info("[ADMIN_BOOTSTRAP] SuperAdmin {} - Email: {} | Token: {} | isRoot: {} | active: {}", 
            wasCreated ? "CREATO" : "ESISTENTE",
            superAdmin.getEmail(),
            superAdmin.getAccessToken() != null ? superAdmin.getAccessToken() : "NULL",
            superAdmin.isRoot(),
            superAdmin.isActive());
    }

    /**
     * 🔑 Genera un token univoco di 128 caratteri (pubblico per uso esterno)
     * Usa UUID + SHA256 per garantire unicità e lunghezza fissa
     */
    public String generateUserToken() {
        return generateToken();
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

    /**
     * 🔍 Controllo diagnostico: lista tutti gli utenti admin e verifica/corregge Super Admin
     */
    public Map<String, Object> diagnosticCheck() {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // Lista tutti gli utenti admin
        List<AdminUser> allUsers = repository.findAll();
        List<Map<String, Object>> usersList = allUsers.stream()
                .map(user -> {
                    Map<String, Object> userMap = new LinkedHashMap<>();
                    userMap.put("id", user.getId().toString());
                    userMap.put("name", user.getName());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole());
                    userMap.put("active", user.isActive());
                    String tokenPreview = user.getAccessToken() != null && user.getAccessToken().length() >= 12
                        ? user.getAccessToken().substring(0, 12) + "..."
                        : "***";
                    userMap.put("accessToken", tokenPreview);
                    return userMap;
                })
                .collect(Collectors.toList());
        
        result.put("totalUsers", allUsers.size());
        result.put("users", usersList);
        
        // Verifica Super Admin
        Optional<AdminUser> superAdminOpt = repository.findFirstByRoleAndActiveTrue("SUPER_ADMIN");
        
        if (superAdminOpt.isEmpty()) {
            // Cerca anche Super Admin inattivo
            Optional<AdminUser> inactiveSuperAdmin = repository.findAll().stream()
                    .filter(u -> "SUPER_ADMIN".equals(u.getRole()))
                    .findFirst();
            
            if (inactiveSuperAdmin.isPresent()) {
                AdminUser superAdmin = inactiveSuperAdmin.get();
                if (!superAdmin.isActive()) {
                    logger.info("🔄 Super Admin trovato ma inattivo. Attivazione in corso...");
                    superAdmin.setActive(true);
                    repository.save(superAdmin);
                    result.put("action", "Super Admin attivato (era inattivo)");
                }
            } else {
                logger.info("📋 Tabella admin_users vuota o Super Admin non trovato. Creazione in corso...");
                ensureSuperAdminExists();
                result.put("action", "Super Admin creato");
            }
        } else {
            AdminUser superAdmin = superAdminOpt.get();
            result.put("action", "Super Admin già presente e attivo");
            
            // Stampa log finale
            String tokenPreview = superAdmin.getAccessToken() != null && superAdmin.getAccessToken().length() >= 10
                ? superAdmin.getAccessToken().substring(0, 10) + "..."
                : "***";
            
            logger.info("✅ SUPER_ADMIN attivo");
            logger.info("   Email: {}", superAdmin.getEmail());
            logger.info("   Token: {}", tokenPreview);
        }
        
        // Ricarica Super Admin per mostrarlo nel risultato
        Optional<AdminUser> finalSuperAdmin = repository.findFirstByRoleAndActiveTrue("SUPER_ADMIN");
        if (finalSuperAdmin.isPresent()) {
            AdminUser sa = finalSuperAdmin.get();
            Map<String, Object> superAdminInfo = new LinkedHashMap<>();
            superAdminInfo.put("id", sa.getId().toString());
            superAdminInfo.put("name", sa.getName());
            superAdminInfo.put("email", sa.getEmail());
            superAdminInfo.put("role", sa.getRole());
            superAdminInfo.put("active", sa.isActive());
            String tokenPreview = sa.getAccessToken() != null && sa.getAccessToken().length() >= 12
                ? sa.getAccessToken().substring(0, 12) + "..."
                : "***";
            superAdminInfo.put("accessToken", tokenPreview);
            result.put("superAdmin", superAdminInfo);
        }
        
        return result;
    }

    /**
     * 🔧 Verifica e corregge il Super Admin con i valori specifici
     * Se la tabella è vuota, crea il Super Admin.
     * Se esiste ma active = false, imposta active = true.
     * Se esiste ma email o token non corrispondono, li aggiorna.
     * 
     * @param expectedToken Token atteso per il Super Admin
     */
    @Transactional
    public Map<String, Object> verifyAndFixSuperAdmin(String expectedToken) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // Lista tutti i record
        List<AdminUser> allUsers = repository.findAll();
        List<Map<String, Object>> usersList = allUsers.stream()
                .map(user -> {
                    Map<String, Object> userMap = new LinkedHashMap<>();
                    userMap.put("id", user.getId().toString());
                    userMap.put("name", user.getName());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole());
                    userMap.put("active", user.isActive());
                    String tokenPreview = user.getAccessToken() != null && user.getAccessToken().length() >= 12
                        ? user.getAccessToken().substring(0, 12) + "..."
                        : "***";
                    userMap.put("accessToken", tokenPreview);
                    return userMap;
                })
                .collect(Collectors.toList());
        
        result.put("totalUsers", allUsers.size());
        result.put("users", usersList);
        
        // Verifica se la tabella è vuota
        if (allUsers.isEmpty()) {
            logger.info("📋 Tabella admin_users vuota. Creazione Super Admin...");
            // Usa il token fornito
            ensureSuperAdminExists(expectedToken);
            result.put("action", "Super Admin creato (tabella era vuota)");
        } else {
            // Cerca Super Admin (attivo o inattivo)
            Optional<AdminUser> superAdminOpt = repository.findAll().stream()
                    .filter(u -> "SUPER_ADMIN".equals(u.getRole()))
                    .findFirst();
            
            if (superAdminOpt.isEmpty()) {
                logger.info("📋 Super Admin non trovato. Creazione...");
                // Usa il token fornito
                ensureSuperAdminExists(expectedToken);
                result.put("action", "Super Admin creato (non esisteva)");
            } else {
                AdminUser superAdmin = superAdminOpt.get();
                boolean updated = false;
                
                // Se active = false, imposta active = true
                if (!superAdmin.isActive()) {
                    logger.info("🔄 Super Admin trovato ma inattivo. Attivazione...");
                    superAdmin.setActive(true);
                    updated = true;
                }
                
                // Se email non corrisponde, aggiorna
                if (!"colonoroberto@gmail.com".equals(superAdmin.getEmail())) {
                    logger.info("📧 Email Super Admin non corrisponde. Aggiornamento da '{}' a 'colonoroberto@gmail.com'", superAdmin.getEmail());
                    superAdmin.setEmail("colonoroberto@gmail.com");
                    updated = true;
                }
                
                // Se token non corrisponde, aggiorna
                if (!expectedToken.equals(superAdmin.getAccessToken())) {
                    logger.info("🔑 Token Super Admin non corrisponde. Aggiornamento...");
                    superAdmin.setAccessToken(expectedToken);
                    updated = true;
                }
                
                // Assicura che name e role siano corretti
                if (!"Will".equals(superAdmin.getName())) {
                    superAdmin.setName("Will");
                    updated = true;
                }
                if (!"SUPER_ADMIN".equals(superAdmin.getRole())) {
                    superAdmin.setRole("SUPER_ADMIN");
                    updated = true;
                }
                
                if (updated) {
                    repository.save(superAdmin);
                    result.put("action", "Super Admin aggiornato");
                } else {
                    result.put("action", "Super Admin già corretto");
                }
            }
        }
        
        // Ricarica e mostra Super Admin finale
        Optional<AdminUser> finalSuperAdmin = repository.findFirstByRoleAndActiveTrue("SUPER_ADMIN");
        if (finalSuperAdmin.isPresent()) {
            AdminUser sa = finalSuperAdmin.get();
            Map<String, Object> superAdminInfo = new LinkedHashMap<>();
            superAdminInfo.put("id", sa.getId().toString());
            superAdminInfo.put("name", sa.getName());
            superAdminInfo.put("email", sa.getEmail());
            superAdminInfo.put("role", sa.getRole());
            superAdminInfo.put("active", sa.isActive());
            String tokenPreview = sa.getAccessToken() != null && sa.getAccessToken().length() >= 12
                ? sa.getAccessToken().substring(0, 12) + "..."
                : "***";
            superAdminInfo.put("accessToken", tokenPreview);
            result.put("superAdmin", superAdminInfo);
            
            // Stampa log nel formato richiesto
            String tokenLogPreview = sa.getAccessToken() != null && sa.getAccessToken().length() >= 10
                ? sa.getAccessToken().substring(0, 10) + "..."
                : "***";
            logger.info("✅ SUPER_ADMIN attivo");
            logger.info("   Email: {}", sa.getEmail());
            logger.info("   Token: {}", tokenLogPreview);
        }
        
        return result;
    }
}

