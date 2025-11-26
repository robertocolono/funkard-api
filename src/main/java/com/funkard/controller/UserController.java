package com.funkard.controller;

import com.funkard.dto.UserDTO;
import com.funkard.dto.UserProfileDTO;
import com.funkard.dto.CookiePreferencesDTO;
import com.funkard.dto.UserPreferencesDTO;
import com.funkard.model.User;
import com.funkard.model.UserAddress;
import com.funkard.service.UserService;
import com.funkard.service.UserAddressService;
import com.funkard.service.UserPreferencesService;
import com.funkard.service.CookieLogExportService;
import com.funkard.service.UserAccountDeletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 👤 Controller per gestione utenti e profili
 * 
 * ✅ Endpoint per profilo utente (/api/user/me)
 * ✅ Gestione indirizzi utente (/api/user/address)
 * ✅ Sicurezza integrata
 * ✅ Validazione completa
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
}, allowCredentials = "true")
public class UserController {

    private final UserService userService;
    private final UserAddressService addressService;
    private final UserPreferencesService preferencesService;
    private final com.funkard.repository.UserRepository userRepository;
    private final CookieLogExportService exportService;
    private final UserAccountDeletionService deletionService;

    // ==================== PROFILO UTENTE ====================

    /**
     * 👤 GET /api/user/me
     * Ottieni profilo utente corrente
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getProfile(@RequestHeader("X-User-Id") String userId) {
        log.info("Richiesta profilo utente: {}", userId);
        
        try {
            User user = userService.findById(Long.parseLong(userId));
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            UserProfileDTO profile = userService.getUserProfile(user);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Errore nel recupero profilo utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ✏️ PUT /api/user/me
     * Aggiorna profilo utente
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UserProfileDTO dto) {
        log.info("Aggiornamento profilo utente: {}", userId);
        
        try {
            // Validazione lingua se fornita
            if (dto.getLanguage() != null && !dto.getLanguage().trim().isEmpty()) {
                if (!LanguageWhitelist.isValid(dto.getLanguage())) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Lingua non supportata: " + dto.getLanguage()));
                }
            }
            
            User user = userService.findById(Long.parseLong(userId));
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            UserProfileDTO updated = userService.updateUserProfile(user, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("Richiesta non valida per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore nell'aggiornamento profilo utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    // ==================== INDIRIZZI UTENTE ====================

    /**
     * 🏠 GET /api/user/address
     * Ottieni tutti gli indirizzi dell'utente
     */
    @GetMapping("/address")
    public ResponseEntity<List<UserAddress>> getAddresses(@RequestHeader("X-User-Id") String userId) {
        log.info("Richiesta indirizzi per utente: {}", userId);
        
        try {
            User user = userService.findById(Long.parseLong(userId));
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<UserAddress> addresses = addressService.getAddresses(user);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            log.error("Errore nel recupero indirizzi per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ➕ POST /api/user/address
     * Aggiungi nuovo indirizzo
     */
    @PostMapping("/address")
    public ResponseEntity<?> addAddress(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UserAddress address) {
        log.info("Aggiunta indirizzo per utente: {}", userId);
        
        try {
            User user = userService.findById(Long.parseLong(userId));
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            UserAddress saved = addressService.addAddress(user, address);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalStateException e) {
            log.warn("Limite indirizzi raggiunto per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore nell'aggiunta indirizzo per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    /**
     * ✏️ PUT /api/user/address/{id}
     * Aggiorna indirizzo esistente
     */
    @PutMapping("/address/{id}")
    public ResponseEntity<?> updateAddress(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long id,
            @Valid @RequestBody UserAddress address) {
        log.info("Aggiornamento indirizzo {} per utente: {}", id, userId);
        
        try {
            User user = userService.findById(Long.parseLong(userId));
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            UserAddress updated = addressService.updateAddress(id, user, address);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.warn("Indirizzo non trovato o non autorizzato per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Errore nell'aggiornamento indirizzo {} per utente {}: {}", id, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    /**
     * 🗑️ DELETE /api/user/address/{id}
     * Elimina indirizzo
     */
    @DeleteMapping("/address/{id}")
    public ResponseEntity<?> deleteAddress(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long id) {
        log.info("Eliminazione indirizzo {} per utente: {}", id, userId);
        
        try {
            User user = userService.findById(Long.parseLong(userId));
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            addressService.deleteAddress(id, user);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Indirizzo non trovato o non autorizzato per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Errore nell'eliminazione indirizzo {} per utente {}: {}", id, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    /**
     * 🎯 PATCH /api/user/address/{id}/default
     * Imposta indirizzo predefinito
     */
    @PatchMapping("/address/{id}/default")
    public ResponseEntity<?> setDefaultAddress(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long id) {
        log.info("Impostazione indirizzo predefinito {} per utente: {}", id, userId);
        
        try {
            User user = userService.findById(Long.parseLong(userId));
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            UserAddress updated = addressService.setDefaultAddress(id, user);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.warn("Indirizzo non trovato o non autorizzato per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Errore nell'impostazione indirizzo predefinito {} per utente {}: {}", id, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    /**
     * 🏠 GET /api/user/address/default
     * Ottieni indirizzo predefinito
     */
    @GetMapping("/address/default")
    public ResponseEntity<UserAddress> getDefaultAddress(@RequestHeader("X-User-Id") String userId) {
        log.info("Richiesta indirizzo predefinito per utente: {}", userId);
        
        try {
            User user = userService.findById(Long.parseLong(userId));
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            return addressService.getDefaultAddress(user)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Errore nel recupero indirizzo predefinito per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ENDPOINT LEGACY (per compatibilità) ====================

    /**
     * 📋 GET /api/users
     * Ottieni tutti gli utenti (admin only)
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Richiesta lista utenti");
        return ResponseEntity.ok(userService.getAll());
    }

    /**
     * ➕ POST /api/users
     * Crea nuovo utente (admin only)
     */
    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody User user) {
        log.info("Creazione nuovo utente");
        UserDTO created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 🗑️ DELETE /api/users/{id}
     * Elimina utente (admin only)
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Eliminazione utente: {}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PREFERENZE UTENTE E COOKIE ====================

    /**
     * 🍪 GET /api/user/preferences
     * Ottieni preferenze utente (cookie e altre preferenze)
     * 
     * Supporta autenticazione via:
     * - JWT Bearer token (header Authorization)
     * - X-User-Id header (legacy)
     */
    @GetMapping("/preferences")
    public ResponseEntity<CookiePreferencesDTO> getPreferences(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Authentication authentication) {
        
        User user = getUserFromRequest(userId, authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Richiesta preferenze per utente: {}", user.getId());
        
        try {
            CookiePreferencesDTO preferences = preferencesService.getPreferencesDTO(user);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            log.error("Errore nel recupero preferenze per utente {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 💾 PUT /api/user/preferences
     * Salva o aggiorna preferenze utente (cookie e altre preferenze)
     * 
     * Se l'utente è loggato, sincronizza con backend per tracciabilità GDPR.
     * Se non loggato, il frontend usa solo localStorage.
     * 
     * Supporta autenticazione via:
     * - JWT Bearer token (header Authorization)
     * - X-User-Id header (legacy)
     */
    @PutMapping("/preferences")
    public ResponseEntity<CookiePreferencesDTO> savePreferences(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody CookiePreferencesDTO dto,
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request) {
        
        User user = getUserFromRequest(userId, authentication);
        if (user == null) {
            // Se non autenticato, restituisci 401 ma permette al frontend di usare localStorage
            log.debug("Richiesta preferenze da utente non autenticato - uso localStorage");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Salvataggio preferenze per utente: {}", user.getId());
        
        try {
            // Estrai IP e UserAgent dalla request
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            preferencesService.saveCookiePreferences(user, dto, ipAddress, userAgent);
            CookiePreferencesDTO updated = preferencesService.getPreferencesDTO(user);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Errore nel salvataggio preferenze per utente {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ⚙️ PATCH /api/user/preferences
     * Aggiorna preferenze utente (language e preferredCurrency)
     * 
     * Aggiorna solo i campi forniti nel payload.
     * Supporta autenticazione via:
     * - JWT Bearer token (header Authorization)
     * - X-User-Id header (legacy)
     */
    @PatchMapping("/preferences")
    public ResponseEntity<?> updatePreferences(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody UserPreferencesDTO dto,
            Authentication authentication) {
        
        User user = getUserFromRequest(userId, authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Aggiornamento preferenze per utente: {}", user.getId());
        
        try {
            // Aggiorna solo i campi forniti
            boolean updated = false;
            
            if (dto.getLanguage() != null && !dto.getLanguage().trim().isEmpty()) {
                // Validazione lingua contro whitelist
                if (!LanguageWhitelist.isValid(dto.getLanguage())) {
                    Map<String, Object> errorMap = new java.util.HashMap<>();
                    errorMap.put("error", "Lingua non supportata: " + dto.getLanguage());
                    return ResponseEntity.badRequest().body(errorMap);
                }
                // Valida formato lingua (es. en, it, es, de, fr)
                if (dto.getLanguage().length() <= 5) {
                    user.setLanguage(dto.getLanguage().toLowerCase());
                    updated = true;
                } else {
                    return ResponseEntity.badRequest().build();
                }
            }
            
            if (dto.getPreferredCurrency() != null && !dto.getPreferredCurrency().trim().isEmpty()) {
                // Valida formato valuta (es. EUR, USD, GBP)
                if (dto.getPreferredCurrency().length() == 3) {
                    // Verifica valuta supportata usando whitelist centralizzata
                    if (com.funkard.config.SupportedCurrencies.isValid(dto.getPreferredCurrency())) {
                        user.setPreferredCurrency(dto.getPreferredCurrency().toUpperCase());
                        updated = true;
                    } else {
                        Map<String, Object> errorMap = new java.util.HashMap<>();
                        errorMap.put("error", "Valuta non supportata: " + dto.getPreferredCurrency());
                        return ResponseEntity.badRequest().body(errorMap);
                    }
                } else {
                    Map<String, Object> errorMap = new java.util.HashMap<>();
                    errorMap.put("error", "Formato valuta non valido. Deve essere un codice di 3 caratteri (es. EUR, USD)");
                    return ResponseEntity.badRequest().body(errorMap);
                }
            }
            
            if (updated) {
                user.setUpdatedAt(java.time.LocalDateTime.now());
                userRepository.save(user);
                
                UserPreferencesDTO response = new UserPreferencesDTO();
                response.setLanguage(user.getLanguage());
                response.setPreferredCurrency(user.getPreferredCurrency());
                return ResponseEntity.ok(response);
            } else {
                // Nessun campo da aggiornare
                UserPreferencesDTO response = new UserPreferencesDTO();
                response.setLanguage(user.getLanguage());
                response.setPreferredCurrency(user.getPreferredCurrency());
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("Errore nell'aggiornamento preferenze per utente {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🍪 GET /api/user/preferences/cookies
     * Ottieni preferenze cookie dell'utente autenticato
     * 
     * ⚠️ RICHIEDE AUTENTICAZIONE OBBLIGATORIA (solo JWT)
     * Utenti anonimi devono usare solo localStorage
     */
    @GetMapping("/preferences/cookies")
    public ResponseEntity<?> getCookiePreferences(Authentication authentication) {
        // Validazione autenticazione obbligatoria
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.warn("Tentativo di accesso a preferenze cookie senza autenticazione");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta per accedere alle preferenze cookie"));
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername());
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Utente non trovato"));
        }
        
        log.info("Richiesta preferenze cookie per utente autenticato: {}", user.getId());
        
        try {
            CookiePreferencesDTO preferences = preferencesService.getPreferencesDTO(user);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            log.error("Errore nel recupero preferenze cookie per utente {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    /**
     * 💾 POST /api/user/preferences/cookies
     * Salva o aggiorna preferenze cookie dell'utente autenticato
     * 
     * ⚠️ RICHIEDE AUTENTICAZIONE OBBLIGATORIA (solo JWT)
     * Utenti anonimi devono usare solo localStorage
     * 
     * Body JSON:
     * {
     *   "cookiesAccepted": true,
     *   "cookiesPreferences": {
     *     "essential": true,
     *     "analytics": false,
     *     "functional": true,
     *     "marketing": false
     *   }
     * }
     */
    @PostMapping("/preferences/cookies")
    public ResponseEntity<?> saveCookiePreferences(
            @Valid @RequestBody CookiePreferencesDTO dto,
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request) {
        
        // Validazione autenticazione obbligatoria
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.warn("Tentativo di salvataggio preferenze cookie senza autenticazione");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta per salvare le preferenze cookie"));
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername());
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Utente non trovato"));
        }
        
        // Estrai IP e UserAgent dalla request
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        log.info("Salvataggio preferenze cookie per utente autenticato: {}", user.getId());
        
        try {
            preferencesService.saveCookiePreferences(user, dto, ipAddress, userAgent);
            CookiePreferencesDTO updated = preferencesService.getPreferencesDTO(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "Preferenze cookie aggiornate correttamente",
                "preferences", updated
            ));
        } catch (Exception e) {
            log.error("Errore nel salvataggio preferenze cookie per utente {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 📥 GET /api/user/preferences/cookies/export
     * Esporta storico completo log consenso cookie (GDPR Art. 15 e 20)
     * 
     * ⚠️ RICHIEDE AUTENTICAZIONE OBBLIGATORIA (solo JWT)
     * L'utente può scaricare solo i propri log
     * 
     * Query Parameters:
     * - format: "json" o "pdf" (default: json)
     */
    @GetMapping("/preferences/cookies/export")
    public ResponseEntity<?> exportCookieLogs(
            @RequestParam(value = "format", defaultValue = "json") String format,
            Authentication authentication) {
        
        // Validazione autenticazione obbligatoria
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername());
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Utente non trovato"));
        }
        
        log.info("Richiesta export log cookie per utente: {} - formato: {}", user.getId(), format);
        
        try {
            if ("pdf".equalsIgnoreCase(format)) {
                byte[] pdfBytes = exportService.exportAsPdf(user);
                return ResponseEntity.ok()
                        .header("Content-Type", "application/pdf")
                        .header("Content-Disposition", "attachment; filename=\"cookie-consent-log-" + user.getId() + ".pdf\"")
                        .body(pdfBytes);
            } else {
                // Default: JSON
                String json = exportService.exportAsJson(user);
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json")
                        .header("Content-Disposition", "attachment; filename=\"cookie-consent-log-" + user.getId() + ".json\"")
                        .body(json);
            }
        } catch (Exception e) {
            log.error("Errore nell'export log cookie per utente {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server: " + e.getMessage()));
        }
    }
    
    /**
     * 🔍 Helper per estrarre IP address dalla request
     */
    private String getClientIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 🗑️ DELETE /api/user/delete-account
     * Richiedi cancellazione account (GDPR Art. 17 - Diritto all'oblio)
     * 
     * ⚠️ RICHIEDE AUTENTICAZIONE OBBLIGATORIA (solo JWT)
     * L'account verrà cancellato definitivamente dopo 7 giorni
     * 
     * Request Body (opzionale):
     * {
     *   "reason": "Motivo cancellazione (opzionale)"
     * }
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(
            @RequestBody(required = false) Map<String, String> request,
            Authentication authentication) {
        
        // Validazione autenticazione obbligatoria
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.warn("Tentativo di cancellazione account senza autenticazione");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta per cancellare l'account"));
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername());
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Utente non trovato"));
        }
        
        // Verifica se esiste già una richiesta
        if (deletionService.hasPendingDeletionRequest(user.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Richiesta di cancellazione già presente per questo account"));
        }
        
        log.info("Richiesta cancellazione account per utente: {} ({})", user.getId(), user.getEmail());
        
        try {
            String reason = request != null ? request.get("reason") : null;
            com.funkard.model.UserDeletion deletion = deletionService.requestAccountDeletion(user, reason);
            
            return ResponseEntity.ok(Map.of(
                "message", "Richiesta di cancellazione account registrata con successo",
                "scheduledDeletionAt", deletion.getScheduledDeletionAt().toString(),
                "note", "Il tuo account verrà cancellato definitivamente dopo 7 giorni. " +
                        "Durante questo periodo non potrai accedere al sistema."
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore nella richiesta cancellazione account per utente {}: {}", 
                user.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    /**
     * 🔍 Helper method per ottenere User da request
     * Supporta sia JWT Bearer token che X-User-Id header
     */
    private User getUserFromRequest(String userId, Authentication authentication) {
        // Prova con JWT Bearer token (preferito)
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByEmail(userDetails.getUsername());
            if (user != null) {
                return user;
            }
        }
        
        // Fallback a X-User-Id header (legacy)
        if (userId != null && !userId.isEmpty()) {
            try {
                return userService.findById(Long.parseLong(userId));
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID format: {}", userId);
            }
        }
        
        return null;
    }
}
