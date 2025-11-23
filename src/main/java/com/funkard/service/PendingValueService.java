package com.funkard.service;

import com.funkard.model.PendingValue;
import com.funkard.model.User;
import com.funkard.repository.PendingValueRepository;
import com.funkard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ⏳ Servizio per gestione valori personalizzati "Altro"
 * 
 * Gestisce proposte di nuovi valori TCG o Lingua che richiedono
 * approvazione admin prima di essere aggiunti alle liste ufficiali.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PendingValueService {
    
    private final PendingValueRepository pendingValueRepository;
    private final UserRepository userRepository;
    
    /**
     * 📝 Crea una nuova proposta di valore personalizzato
     * 
     * @param type Tipo valore (TCG o LANGUAGE)
     * @param value Valore proposto
     * @param userId ID utente che propone
     * @return PendingValue creato
     */
    @Transactional
    public PendingValue submitPendingValue(PendingValue.ValueType type, String value, Long userId) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Il valore non può essere vuoto");
        }
        
        // Normalizza valore (trim, capitalize)
        String normalizedValue = normalizeValue(value);
        
        // Verifica se esiste già una proposta identica
        Optional<PendingValue> existing = pendingValueRepository.findByTypeAndValueIgnoreCase(type, normalizedValue);
        if (existing.isPresent()) {
            PendingValue existingValue = existing.get();
            if (existingValue.getApproved()) {
                log.info("⚠️ Valore già approvato: {} - {}", type, normalizedValue);
                throw new IllegalStateException("Questo valore è già stato approvato e aggiunto alle liste ufficiali");
            } else {
                log.info("⚠️ Proposta già esistente (pending): {} - {}", type, normalizedValue);
                throw new IllegalStateException("Una proposta identica è già in attesa di approvazione");
            }
        }
        
        // Recupera utente
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
        
        // Crea proposta
        PendingValue pendingValue = new PendingValue();
        pendingValue.setType(type);
        pendingValue.setValue(normalizedValue);
        pendingValue.setSubmittedBy(user);
        pendingValue.setCreatedAt(LocalDateTime.now());
        pendingValue.setApproved(false);
        
        PendingValue saved = pendingValueRepository.save(pendingValue);
        log.info("✅ Proposta valore creata: {} - {} (utente: {})", type, normalizedValue, userId);
        
        return saved;
    }
    
    /**
     * ✅ Approva una proposta di valore
     * 
     * @param pendingValueId ID proposta
     * @param adminId ID admin che approva
     * @return PendingValue approvato
     */
    @Transactional
    public PendingValue approvePendingValue(UUID pendingValueId, Long adminId) {
        PendingValue pendingValue = pendingValueRepository.findById(pendingValueId)
            .orElseThrow(() -> new IllegalArgumentException("Proposta non trovata"));
        
        if (pendingValue.getApproved()) {
            throw new IllegalStateException("Questa proposta è già stata approvata");
        }
        
        // Recupera admin
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new IllegalArgumentException("Admin non trovato"));
        
        // Verifica che sia admin
        if (!admin.getRole().equals("ADMIN") && !admin.getRole().equals("SUPER_ADMIN") && !admin.getRole().equals("SUPERVISOR")) {
            throw new IllegalStateException("Solo admin possono approvare proposte");
        }
        
        // Approva
        pendingValue.setApproved(true);
        pendingValue.setApprovedBy(admin);
        pendingValue.setApprovedAt(LocalDateTime.now());
        
        PendingValue saved = pendingValueRepository.save(pendingValue);
        log.info("✅ Proposta approvata: {} - {} (admin: {})", 
            pendingValue.getType(), pendingValue.getValue(), adminId);
        
        // TODO: Aggiungere valore alle liste ufficiali TCG/Lingua
        // Questo richiederà una tabella dedicata o configurazione esterna
        
        return saved;
    }
    
    /**
     * ❌ Rifiuta una proposta di valore
     * 
     * @param pendingValueId ID proposta
     * @param adminId ID admin che rifiuta
     */
    @Transactional
    public void rejectPendingValue(UUID pendingValueId, Long adminId) {
        PendingValue pendingValue = pendingValueRepository.findById(pendingValueId)
            .orElseThrow(() -> new IllegalArgumentException("Proposta non trovata"));
        
        if (pendingValue.getApproved()) {
            throw new IllegalStateException("Questa proposta è già stata approvata");
        }
        
        // Elimina proposta (o marca come rifiutata)
        pendingValueRepository.delete(pendingValue);
        log.info("❌ Proposta rifiutata: {} - {} (admin: {})", 
            pendingValue.getType(), pendingValue.getValue(), adminId);
    }
    
    /**
     * 📋 Recupera tutte le proposte pending
     */
    public List<PendingValue> getPendingValues() {
        return pendingValueRepository.findByApprovedFalseOrderByCreatedAtDesc();
    }
    
    /**
     * 📋 Recupera proposte pending per tipo
     */
    public List<PendingValue> getPendingValuesByType(PendingValue.ValueType type) {
        return pendingValueRepository.findByTypeAndApprovedFalseOrderByCreatedAtDesc(type);
    }
    
    /**
     * 📋 Recupera proposte di un utente
     */
    public List<PendingValue> getUserPendingValues(Long userId) {
        return pendingValueRepository.findBySubmittedByIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 📊 Conta proposte pending per tipo
     */
    public long countPendingByType(PendingValue.ValueType type) {
        return pendingValueRepository.countByTypeAndApprovedFalse(type);
    }
    
    /**
     * 🔍 Normalizza valore (trim, capitalize prima lettera)
     */
    private String normalizeValue(String value) {
        if (value == null) {
            return null;
        }
        
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        
        // Capitalizza prima lettera
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}

