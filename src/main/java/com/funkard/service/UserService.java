package com.funkard.service;

import com.funkard.model.User;
import com.funkard.repository.UserRepository;
import com.funkard.dto.UserDTO;
import com.funkard.dto.UserProfileDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 👤 Service per gestione utenti e profili
 * 
 * ✅ CRUD completo utenti
 * ✅ Gestione profili utente
 * ✅ Validazioni business
 * ✅ Transazioni ottimizzate
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository repo;

    // ==================== CRUD UTENTI ====================

    /**
     * 📋 Ottieni tutti gli utenti
     */
    public List<UserDTO> getAll() {
        log.info("Recupero lista utenti");
        return repo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * ➕ Crea nuovo utente
     */
    @Transactional
    public UserDTO create(User user) {
        log.info("Creazione nuovo utente: {}", user.getEmail());
        
        // Validazioni business
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email obbligatoria");
        }
        
        if (repo.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email già registrata");
        }

        // Imposta valori di default
        if (user.getPreferredCurrency() == null) {
            user.setPreferredCurrency("EUR");
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }

        User saved = repo.save(user);
        log.info("Utente creato con successo: {}", saved.getId());
        return toDTO(saved);
    }

    /**
     * 🔍 Trova utente per ID
     */
    public User findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    /**
     * 🔍 Trova utente per email
     */
    public User findByEmail(String email) {
        return repo.findByEmail(email);
    }

    /**
     * 🗑️ Elimina utente
     */
    @Transactional
    public void delete(Long id) {
        log.info("Eliminazione utente: {}", id);
        
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Utente non trovato");
        }
        
        repo.deleteById(id);
        log.info("Utente eliminato con successo: {}", id);
    }

    // ==================== GESTIONE PROFILI ====================

    /**
     * 👤 Ottieni profilo utente
     */
    public UserProfileDTO getUserProfile(User user) {
        log.info("Recupero profilo utente: {}", user.getId());
        
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setPreferredCurrency(user.getPreferredCurrency());
        dto.setLanguage(user.getLanguage());
        dto.setTheme(user.getTheme());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setDescriptionOriginal(user.getDescriptionOriginal());
        dto.setDescriptionLanguage(user.getDescriptionLanguage());
        
        return dto;
    }

    /**
     * ✏️ Aggiorna profilo utente
     */
    @Transactional
    public UserProfileDTO updateUserProfile(User user, UserProfileDTO dto) {
        log.info("Aggiornamento profilo utente: {}", user.getId());
        
        // Validazioni
        if (dto.getName() != null && dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        
        // 💱 Fallback a USD se currency è null
        if (dto.getPreferredCurrency() == null || dto.getPreferredCurrency().trim().isEmpty()) {
            dto.setPreferredCurrency("USD");
        }
        
        if (dto.getPreferredCurrency() != null && !isValidCurrency(dto.getPreferredCurrency())) {
            throw new IllegalArgumentException("Valuta non supportata: " + dto.getPreferredCurrency());
        }
        
        // Validazione bio venditore (max 500 caratteri)
        if (dto.getDescriptionOriginal() != null && dto.getDescriptionOriginal().length() > 500) {
            throw new IllegalArgumentException("La bio del venditore non può superare 500 caratteri.");
        }

        // Aggiorna solo i campi forniti
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getPreferredCurrency() != null) {
            user.setPreferredCurrency(dto.getPreferredCurrency());
        }
        if (dto.getLanguage() != null) {
            user.setLanguage(dto.getLanguage());
        }
        if (dto.getTheme() != null) {
            user.setTheme(dto.getTheme());
        }
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        if (dto.getDescriptionOriginal() != null) {
            user.setDescriptionOriginal(dto.getDescriptionOriginal());
        }
        if (dto.getDescriptionLanguage() != null) {
            user.setDescriptionLanguage(dto.getDescriptionLanguage());
        }

        // Aggiorna timestamp
        user.setUpdatedAt(LocalDateTime.now());
        
        User saved = repo.save(user);
        log.info("Profilo aggiornato con successo: {}", saved.getId());
        
        return getUserProfile(saved);
    }

    /**
     * 🔄 Aggiorna ultimo accesso
     */
    @Transactional
    public void updateLastLogin(Long userId) {
        log.debug("Aggiornamento ultimo accesso per utente: {}", userId);
        
        repo.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            repo.save(user);
        });
    }

    // ==================== UTILITY METHODS ====================

    /**
     * ✅ Verifica se una valuta è supportata
     * Usa la whitelist centralizzata SupportedCurrencies
     */
    private boolean isValidCurrency(String currency) {
        return com.funkard.config.SupportedCurrencies.isValid(currency);
    }

    /**
     * 🔄 Converte User in UserDTO
     */
    private UserDTO toDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setUsername(u.getUsername());
        dto.setName(u.getName());
        dto.setAvatarUrl(u.getAvatarUrl());
        dto.setRole(u.getRole());
        dto.setPreferredCurrency(u.getPreferredCurrency());
        return dto;
    }

    /**
     * 📊 Statistiche utenti
     */
    public long getUserCount() {
        return repo.count();
    }

    /**
     * 🔍 Verifica se email esiste
     */
    public boolean emailExists(String email) {
        return repo.findByEmail(email) != null;
    }
}