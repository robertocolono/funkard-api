package com.funkard.market.controller;

import com.funkard.market.model.Product;
import com.funkard.market.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 🛒 Controller per gestione prodotti
 * 
 * Gestisce creazione e lettura prodotti.
 * Supporta campi per traduzione dinamica (descriptionOriginal, descriptionLanguage).
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
}, allowCredentials = "true")
public class ProductController {
    
    private final ProductService productService;
    private final com.funkard.market.repository.ProductRepository productRepository;
    
    /**
     * 📋 GET /api/products
     * Lista tutti i prodotti
     */
    @GetMapping
    @Cacheable(value = "marketplace:search", key = "'all'")
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Errore durante recupero prodotti: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 🔍 GET /api/products/{id}
     * Dettaglio prodotto
     */
    @GetMapping("/{id}")
    @Cacheable(value = "marketplace:search", key = "#id")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        try {
            Product product = productRepository.findById(id)
                .orElse(null);
            
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Errore durante recupero prodotto {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * ➕ POST /api/products
     * Crea nuovo prodotto
     * 
     * Request body accetta:
     * {
     *   "name": "Nome prodotto",
     *   "price": 100.0,
     *   "estimatedValue": 90.0,
     *   "userId": "user123",
     *   "descriptionOriginal": "Descrizione originale in italiano",
     *   "descriptionLanguage": "it",
     *   "nameEn": "Product name in English" // opzionale
     * }
     * 
     * I campi descriptionOriginal e descriptionLanguage sono opzionali (nullable).
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product) {
        try {
            log.info("Creazione prodotto: {} (descriptionLanguage: {})", 
                product.getName(), product.getDescriptionLanguage());
            
            Product saved = productService.createProduct(product);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Errore durante creazione prodotto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Errore interno del server"));
        }
    }
}

