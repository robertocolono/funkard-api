package com.funkard.service;

import com.funkard.model.FranchiseCatalog;
import com.funkard.repository.FranchiseCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 📚 Servizio per gestione catalogo franchise
 * 
 * Gestisce i franchise disponibili per le carte, con possibilità
 * di attivazione/disattivazione dal pannello admin.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FranchiseCatalogService {
    
    private final FranchiseCatalogRepository franchiseRepository;
    
    /**
     * 📋 Recupera tutti i franchise attivi
     * 
     * @return Lista franchise attivi ordinati per categoria e nome
     */
    public List<FranchiseCatalog> getActiveFranchises() {
        return franchiseRepository.findByActiveTrueOrderByCategoryAscNameAsc();
    }
    
    /**
     * 📋 Recupera franchise per categoria (solo attivi)
     * 
     * @param category Categoria (es. "TCG", "Anime", "TCG / Anime")
     * @return Lista franchise per categoria
     */
    public List<FranchiseCatalog> getFranchisesByCategory(String category) {
        return franchiseRepository.findByCategoryAndActiveTrueOrderByNameAsc(category);
    }
    
    /**
     * 📋 Recupera franchise raggruppati per categoria
     * 
     * @return Mappa categoria -> lista franchise
     */
    public Map<String, List<FranchiseCatalog>> getFranchisesGroupedByCategory() {
        List<FranchiseCatalog> franchises = getActiveFranchises();
        return franchises.stream()
            .collect(Collectors.groupingBy(FranchiseCatalog::getCategory));
    }
    
    /**
     * ➕ Crea nuovo franchise (admin)
     * 
     * @param category Categoria
     * @param name Nome franchise
     * @return FranchiseCatalog creato
     */
    @Transactional
    public FranchiseCatalog createFranchise(String category, String name) {
        // Verifica duplicati
        Optional<FranchiseCatalog> existing = franchiseRepository
            .findByCategoryAndNameIgnoreCase(category, name);
        
        if (existing.isPresent()) {
            throw new IllegalStateException(
                String.format("Franchise '%s' già esistente per categoria '%s'", name, category));
        }
        
        FranchiseCatalog franchise = new FranchiseCatalog();
        franchise.setCategory(category);
        franchise.setName(normalizeName(name));
        franchise.setActive(true);
        franchise.setCreatedAt(LocalDateTime.now());
        franchise.setUpdatedAt(LocalDateTime.now());
        
        FranchiseCatalog saved = franchiseRepository.save(franchise);
        log.info("✅ Franchise creato: {} - {}", category, saved.getName());
        
        return saved;
    }
    
    /**
     * ✏️ Aggiorna franchise (admin)
     * 
     * @param id ID franchise
     * @param category Nuova categoria (opzionale)
     * @param name Nuovo nome (opzionale)
     * @param active Nuovo stato attivo (opzionale)
     * @return FranchiseCatalog aggiornato
     */
    @Transactional
    public FranchiseCatalog updateFranchise(Long id, String category, String name, Boolean active) {
        FranchiseCatalog franchise = franchiseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Franchise non trovato"));
        
        if (category != null && !category.trim().isEmpty()) {
            franchise.setCategory(category);
        }
        
        if (name != null && !name.trim().isEmpty()) {
            // Verifica duplicati se nome cambia
            Optional<FranchiseCatalog> existing = franchiseRepository
                .findByCategoryAndNameIgnoreCase(franchise.getCategory(), name);
            
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new IllegalStateException(
                    String.format("Franchise '%s' già esistente per categoria '%s'", name, franchise.getCategory()));
            }
            
            franchise.setName(normalizeName(name));
        }
        
        if (active != null) {
            franchise.setActive(active);
        }
        
        franchise.setUpdatedAt(LocalDateTime.now());
        
        FranchiseCatalog saved = franchiseRepository.save(franchise);
        log.info("✅ Franchise aggiornato: {} - {}", saved.getCategory(), saved.getName());
        
        return saved;
    }
    
    /**
     * 🗑️ Elimina franchise (admin)
     * 
     * @param id ID franchise
     */
    @Transactional
    public void deleteFranchise(Long id) {
        FranchiseCatalog franchise = franchiseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Franchise non trovato"));
        
        franchiseRepository.delete(franchise);
        log.info("🗑️ Franchise eliminato: {} - {}", franchise.getCategory(), franchise.getName());
    }
    
    /**
     * 🔍 Verifica se franchise esiste e è attivo
     * 
     * @param category Categoria
     * @param name Nome franchise
     * @return true se esiste e è attivo
     */
    public boolean isFranchiseActive(String category, String name) {
        Optional<FranchiseCatalog> franchise = franchiseRepository
            .findByCategoryAndNameIgnoreCase(category, name);
        
        return franchise.isPresent() && franchise.get().getActive();
    }
    
    /**
     * 📊 Recupera statistiche franchise
     * 
     * @return Mappa con statistiche
     */
    public Map<String, Object> getStats() {
        long totalActive = franchiseRepository.findByActiveTrueOrderByCategoryAscNameAsc().size();
        long totalInactive = franchiseRepository.findAllByOrderByCategoryAscNameAsc().size() - totalActive;
        
        Map<String, Long> byCategory = franchiseRepository.findByActiveTrueOrderByCategoryAscNameAsc()
            .stream()
            .collect(Collectors.groupingBy(
                FranchiseCatalog::getCategory,
                Collectors.counting()
            ));
        
        return Map.of(
            "totalActive", totalActive,
            "totalInactive", totalInactive,
            "byCategory", byCategory
        );
    }
    
    /**
     * 🔍 Normalizza nome franchise (trim, capitalize)
     */
    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }
        
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        
        // Capitalizza prima lettera
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1);
    }
}

