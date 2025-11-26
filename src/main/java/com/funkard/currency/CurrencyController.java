package com.funkard.currency;

import com.funkard.config.SupportedCurrencies;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 💱 Controller per conversione valute
 * 
 * Endpoint pubblico per testare la conversione tra valute supportate.
 * Utilizza CurrencyConversionService con cache interna (TTL 1 ora).
 */
@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
}, allowCredentials = "true")
public class CurrencyController {
    
    private final CurrencyConversionService conversionService;
    
    /**
     * 💱 GET /api/currency/convert
     * Converte un importo da una valuta a un'altra
     * 
     * Query Parameters:
     * - from: Valuta di origine (es. "USD") - obbligatorio
     * - to: Valuta di destinazione (es. "EUR") - obbligatorio
     * - amount: Importo da convertire (es. 100.0) - obbligatorio
     * 
     * Response:
     * {
     *   "from": "USD",
     *   "to": "EUR",
     *   "amount": 100.0,
     *   "converted": 85.0,
     *   "rate": 0.85
     * }
     * 
     * @param from Valuta di origine
     * @param to Valuta di destinazione
     * @param amount Importo da convertire
     * @return Risultato conversione con tasso di cambio
     */
    @GetMapping("/convert")
    public ResponseEntity<?> convert(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("amount") Double amount) {
        
        log.info("💱 Richiesta conversione: {} {} -> {}", amount, from, to);
        
        try {
            // Validazione parametri obbligatori
            if (from == null || from.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Parametro 'from' obbligatorio"));
            }
            
            if (to == null || to.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Parametro 'to' obbligatorio"));
            }
            
            if (amount == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Parametro 'amount' obbligatorio"));
            }
            
            if (amount < 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "L'importo deve essere positivo"));
            }
            
            // Normalizza valute a uppercase
            String fromCurrency = from.trim().toUpperCase();
            String toCurrency = to.trim().toUpperCase();
            
            // Valida valute supportate
            if (!SupportedCurrencies.isValid(fromCurrency)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", 
                        String.format("Valuta di origine non supportata: '%s'. Valute supportate: EUR, USD, GBP, JPY, BRL, CAD, AUD", fromCurrency)));
            }
            
            if (!SupportedCurrencies.isValid(toCurrency)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", 
                        String.format("Valuta di destinazione non supportata: '%s'. Valute supportate: EUR, USD, GBP, JPY, BRL, CAD, AUD", toCurrency)));
            }
            
            // Esegui conversione
            double converted = conversionService.convert(amount, fromCurrency, toCurrency);
            
            // Calcola tasso di cambio (per risposta)
            double rate = converted / amount;
            
            // Costruisci risposta
            Map<String, Object> response = new HashMap<>();
            response.put("from", fromCurrency);
            response.put("to", toCurrency);
            response.put("amount", amount);
            response.put("converted", converted);
            response.put("rate", rate);
            
            log.info("✅ Conversione completata: {} {} -> {} {} (rate: {})", 
                amount, fromCurrency, converted, toCurrency, rate);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Richiesta non valida: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Richiesta non valida: " + e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Errore durante conversione: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server durante conversione"));
        }
    }
}

