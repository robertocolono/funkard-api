package com.funkard.admin.service;

import com.funkard.admin.model.HumanReadableCounter;
import com.funkard.admin.repository.HumanReadableCounterRepository;
import jakarta.persistence.PessimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 📋 Service per generazione numeri umani leggibili
 * Formato: {PREFIX}-{YYYY}-{NNNN}
 * Esempio: SYS-2025-0001
 * 
 * Thread-safe tramite SELECT FOR UPDATE
 * Lazy creation contatore (on-demand)
 */
@Service
public class HumanReadableNumberService {
    
    private static final Logger log = LoggerFactory.getLogger(HumanReadableNumberService.class);
    private static final int LOCK_TIMEOUT_SECONDS = 5;
    
    private final HumanReadableCounterRepository counterRepo;
    
    public HumanReadableNumberService(HumanReadableCounterRepository counterRepo) {
        this.counterRepo = counterRepo;
    }
    
    /**
     * Genera numero umano per prefix
     * Thread-safe tramite SELECT FOR UPDATE
     * 
     * @param prefix Prefisso tipo entità (es. "SYS")
     * @return Numero umano formato {PREFIX}-{YYYY}-{NNNN} o null se fallisce
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, timeout = LOCK_TIMEOUT_SECONDS)
    public String generateHumanReadableNumber(String prefix) {
        try {
            int year = LocalDate.now().getYear();
            
            // Lock esclusivo su riga contatore (SELECT FOR UPDATE)
            HumanReadableCounter counter = counterRepo
                .findByPrefixAndYearForUpdate(prefix, year)
                .orElseGet(() -> createNewCounter(prefix, year));
            
            // Incrementa contatore
            counter.setCurrentValue(counter.getCurrentValue() + 1);
            counter.setUpdatedAt(LocalDateTime.now());
            counterRepo.save(counter);
            
            // Genera numero umano
            int nextValue = counter.getCurrentValue();
            String humanNumber = String.format("%s-%d-%04d", prefix, year, nextValue);
            
            log.debug("Generated human-readable number: {} for prefix: {}, year: {}", 
                humanNumber, prefix, year);
            
            return humanNumber;
            
        } catch (PessimisticLockException e) {
            log.warn("Lock timeout generating human-readable number for prefix: {}", prefix, e);
            return null;
        } catch (DataAccessException e) {
            log.error("Database error generating human-readable number for prefix: {}", prefix, e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error generating human-readable number for prefix: {}", prefix, e);
            return null;
        }
    }
    
    /**
     * Crea nuovo contatore per (prefix, year)
     * Chiamato solo se contatore non esiste (lazy creation)
     * Gestisce race condition: se due thread creano simultaneamente, uno vince e l'altro recupera quello esistente
     */
    private HumanReadableCounter createNewCounter(String prefix, Integer year) {
        try {
            // Prova a creare nuovo contatore
            HumanReadableCounter newCounter = new HumanReadableCounter(prefix, year);
            newCounter.setCurrentValue(0); // Verrà incrementato a 1
            
            // Salva (unique constraint previene duplicati in race condition)
            HumanReadableCounter saved = counterRepo.save(newCounter);
            
            log.debug("Created new counter for prefix: {}, year: {}", prefix, year);
            return saved;
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Race condition: altro thread ha già creato il contatore
            // Recupera quello esistente con lock
            log.debug("Counter already exists for prefix: {}, year: {} (race condition handled)", prefix, year);
            return counterRepo.findByPrefixAndYearForUpdate(prefix, year)
                .orElseThrow(() -> new IllegalStateException(
                    "Failed to create or retrieve counter for prefix: " + prefix + ", year: " + year));
        } catch (Exception e) {
            log.error("Error creating new counter for prefix: {}, year: {}", prefix, year, e);
            // Se creazione fallisce per altro motivo, prova a recuperare quello esistente
            return counterRepo.findByPrefixAndYearForUpdate(prefix, year)
                .orElseThrow(() -> new IllegalStateException(
                    "Failed to create counter for prefix: " + prefix + ", year: " + year, e));
        }
    }
    
    /**
     * Determina prefix per AdminNotification
     * Solo per type="system" e priority="error"|"warn"
     * 
     * @param type Tipo notifica
     * @param priority Priorità notifica
     * @return Prefix ("SYS") o null se non applicabile
     */
    public String determinePrefixForNotification(String type, String priority) {
        if ("system".equals(type) && ("error".equals(priority) || "warn".equals(priority))) {
            return "SYS";
        }
        return null;
    }
}

