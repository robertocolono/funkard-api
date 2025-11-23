package com.funkard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 📚 Servizio per lettura franchise da file JSON statico
 * 
 * Carica i dati da src/main/resources/data/franchises.json
 * e li mantiene in cache per accesso rapido.
 */
@Service
@Slf4j
public class FranchiseJsonService {
    
    @Value("${franchise.json.path:data/franchises.json}")
    private String jsonFilePath;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<Map<String, Object>> cachedFranchises = new ArrayList<>();
    private boolean dataAvailable = false;
    
    /**
     * 📂 Carica dati da JSON all'avvio
     */
    @PostConstruct
    public void loadFranchisesFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource(jsonFilePath);
            
            if (!resource.exists()) {
                log.warn("⚠️ File franchise JSON non trovato: {}", jsonFilePath);
                dataAvailable = false;
                return;
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> franchises = objectMapper.readValue(
                    inputStream,
                    List.class
                );
                
                cachedFranchises = franchises;
                dataAvailable = true;
                
                log.info("✅ Caricati {} categorie franchise da {}", 
                    franchises.size(), jsonFilePath);
            }
            
        } catch (IOException e) {
            log.error("❌ Errore durante lettura file franchise JSON: {}", e.getMessage(), e);
            dataAvailable = false;
        }
    }
    
    /**
     * 📋 Recupera tutte le categorie e franchise
     * 
     * @return Lista con struttura: [{"category": "...", "franchises": [...]}, ...]
     */
    public List<Map<String, Object>> getAllFranchises() {
        if (!dataAvailable) {
            throw new IllegalStateException("Franchise data unavailable");
        }
        return new ArrayList<>(cachedFranchises);
    }
    
    /**
     * 📋 Recupera franchise per categoria
     * 
     * @param category Categoria (es. "TCG / Anime")
     * @return Lista franchise per categoria, o lista vuota se non trovata
     */
    @SuppressWarnings("unchecked")
    public List<String> getFranchisesByCategory(String category) {
        if (!dataAvailable) {
            throw new IllegalStateException("Franchise data unavailable");
        }
        
        return cachedFranchises.stream()
            .filter(item -> category.equals(item.get("category")))
            .findFirst()
            .map(item -> (List<String>) item.get("franchises"))
            .orElse(new ArrayList<>());
    }
    
    /**
     * 📋 Recupera lista categorie disponibili
     * 
     * @return Lista categorie uniche
     */
    public List<String> getCategories() {
        if (!dataAvailable) {
            throw new IllegalStateException("Franchise data unavailable");
        }
        
        return cachedFranchises.stream()
            .map(item -> (String) item.get("category"))
            .distinct()
            .toList();
    }
    
    /**
     * 🔍 Verifica se categoria esiste
     */
    public boolean categoryExists(String category) {
        if (!dataAvailable) {
            return false;
        }
        
        return cachedFranchises.stream()
            .anyMatch(item -> category.equals(item.get("category")));
    }
    
    /**
     * 🔍 Verifica se franchise esiste in una categoria
     */
    @SuppressWarnings("unchecked")
    public boolean franchiseExists(String category, String franchise) {
        if (!dataAvailable) {
            return false;
        }
        
        return cachedFranchises.stream()
            .filter(item -> category.equals(item.get("category")))
            .findFirst()
            .map(item -> {
                List<String> franchises = (List<String>) item.get("franchises");
                return franchises != null && franchises.contains(franchise);
            })
            .orElse(false);
    }
    
    /**
     * ✅ Verifica se i dati sono disponibili
     */
    public boolean isDataAvailable() {
        return dataAvailable;
    }
    
    /**
     * 🔄 Ricarica dati da JSON (utile per hot-reload in dev)
     */
    public void reload() {
        log.info("🔄 Ricaricamento dati franchise da JSON...");
        loadFranchisesFromJson();
    }
    
    /**
     * 📝 Aggiorna file JSON con nuovo franchise
     * 
     * @param category Categoria
     * @param franchiseName Nome franchise
     * @param add true per aggiungere, false per rimuovere
     * @return true se aggiornato con successo
     */
    public boolean updateJsonFile(String category, String franchiseName, boolean add) {
        try {
            // Ricarica dati attuali
            if (!dataAvailable) {
                loadFranchisesFromJson();
            }
            
            List<Map<String, Object>> franchises = new ArrayList<>(cachedFranchises);
            
            // Trova o crea categoria
            Optional<Map<String, Object>> categoryEntry = franchises.stream()
                .filter(item -> category.equals(item.get("category")))
                .findFirst();
            
            @SuppressWarnings("unchecked")
            List<String> franchiseList;
            
            if (categoryEntry.isPresent()) {
                franchiseList = (List<String>) categoryEntry.get().get("franchises");
                if (franchiseList == null) {
                    franchiseList = new ArrayList<>();
                    categoryEntry.get().put("franchises", franchiseList);
                }
            } else {
                // Crea nuova categoria
                Map<String, Object> newCategory = new HashMap<>();
                newCategory.put("category", category);
                franchiseList = new ArrayList<>();
                newCategory.put("franchises", franchiseList);
                franchises.add(newCategory);
            }
            
            // Aggiungi o rimuovi franchise
            if (add) {
                if (!franchiseList.contains(franchiseName)) {
                    franchiseList.add(franchiseName);
                    franchiseList.sort(String::compareToIgnoreCase);
                }
            } else {
                franchiseList.remove(franchiseName);
            }
            
            // Aggiorna cache
            cachedFranchises = franchises;
            
            log.info("✅ Cache JSON aggiornata ({}): {} - {}", 
                add ? "aggiunto" : "rimosso", category, franchiseName);
            
            // Nota: Il file fisico non viene modificato (è in resources)
            // In produzione, salvare in percorso scrivibile
            return true;
            
        } catch (Exception e) {
            log.error("❌ Errore durante aggiornamento cache JSON: {}", e.getMessage(), e);
            return false;
        }
    }
}

