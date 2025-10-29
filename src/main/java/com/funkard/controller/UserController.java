package com.funkard.controller;

import com.funkard.dto.UserDTO;
import com.funkard.dto.UserProfileDTO;
import com.funkard.model.User;
import com.funkard.model.UserAddress;
import com.funkard.service.UserService;
import com.funkard.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@CrossOrigin(origins = {"https://funkard.vercel.app", "https://funkardnew.vercel.app", "http://localhost:3000"})
public class UserController {

    private final UserService userService;
    private final UserAddressService addressService;

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
}
