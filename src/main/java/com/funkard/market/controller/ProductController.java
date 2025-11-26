package com.funkard.market.controller;

import com.funkard.currency.CurrencyConversionService;
import com.funkard.dto.ProductDTO;
import com.funkard.market.model.Product;
import com.funkard.market.service.ProductService;
import com.funkard.model.User;
import com.funkard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

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
    private final CurrencyConversionService currencyConversionService;
    private final UserRepository userRepository;
    
    /**
     * 📋 GET /api/products
     * Lista tutti i prodotti
     */
    @GetMapping
    @Cacheable(value = "marketplace:search", key = "'all'")
    public ResponseEntity<List<ProductDTO>> getAllProducts(Authentication authentication) {
        try {
            List<Product> products = productRepository.findAll();
            String targetCurrency = getTargetCurrency(authentication);
            
            List<ProductDTO> dtos = products.stream()
                .map(product -> toProductDTO(product, targetCurrency))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
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
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id, Authentication authentication) {
        try {
            Product product = productRepository.findById(id)
                .orElse(null);
            
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            
            String targetCurrency = getTargetCurrency(authentication);
            ProductDTO dto = toProductDTO(product, targetCurrency);
            
            return ResponseEntity.ok(dto);
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
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product, Authentication authentication) {
        try {
            log.info("Creazione prodotto: {} (descriptionLanguage: {})", 
                product.getName(), product.getDescriptionLanguage());
            
            Product saved = productService.createProduct(product);
            
            // Converte a DTO con conversione valuta
            String targetCurrency = getTargetCurrency(authentication);
            ProductDTO dto = toProductDTO(saved, targetCurrency);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
            log.error("Errore durante creazione prodotto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 🔍 Helper per ottenere valuta target (preferredCurrency dell'utente o "USD")
     */
    private String getTargetCurrency(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername());
            if (user != null && user.getPreferredCurrency() != null && !user.getPreferredCurrency().trim().isEmpty()) {
                return user.getPreferredCurrency().toUpperCase();
            }
        }
        return "USD"; // Default per utenti non autenticati
    }
    
    /**
     * 🔄 Converte Product entity a ProductDTO con conversione valuta
     */
    private ProductDTO toProductDTO(Product product, String targetCurrency) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setCurrency(product.getCurrency());
        dto.setEstimatedValue(product.getEstimatedValue());
        dto.setUserId(product.getUserId());
        dto.setDescriptionOriginal(product.getDescriptionOriginal());
        dto.setDescriptionLanguage(product.getDescriptionLanguage());
        dto.setNameEn(product.getNameEn());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        // Calcola convertedPrice e convertedCurrency
        if (product.getPrice() != null && product.getCurrency() != null) {
            try {
                double converted = currencyConversionService.convert(
                    product.getPrice(),
                    product.getCurrency(),
                    targetCurrency
                );
                dto.setConvertedPrice(converted);
                dto.setConvertedCurrency(targetCurrency);
            } catch (Exception e) {
                log.warn("Errore durante conversione valuta per prodotto {}: {}", product.getId(), e.getMessage());
                // In caso di errore, usa il prezzo originale
                dto.setConvertedPrice(product.getPrice());
                dto.setConvertedCurrency(product.getCurrency());
            }
        } else {
            dto.setConvertedPrice(product.getPrice());
            dto.setConvertedCurrency(product.getCurrency());
        }
        
        return dto;
    }
}

