package com.funkard.market.controller;

import com.funkard.market.model.MarketValuation;
import com.funkard.market.service.MarketValuationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/valuation")
@CrossOrigin(origins = {"https://funkard.vercel.app", "https://funkard.com"})
public class MarketValuationController {

    @Autowired
    private MarketValuationService service;

    @PostMapping("/get")
    public ResponseEntity<MarketValuation> getValuation(@RequestBody MarketValuationRequest body) {
        MarketValuation valuation = service.getOrCreateValuation(
                body.itemName(),
                body.setName(),
                body.category(),
                body.condition(),
                body.grade()
        );
        return ResponseEntity.ok(valuation);
    }

    @PostMapping("/refreshIncremental")
    public ResponseEntity<String> refreshIncremental(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // 🔓 Verifica token cron Cloudflare (bypass per cron worker)
        String cronSecret = System.getenv("FUNKARD_CRON_SECRET");
        if (cronSecret == null || cronSecret.isBlank()) {
            cronSecret = System.getProperty("FUNKARD_CRON_SECRET", "");
        }
        cronSecret = cronSecret != null ? cronSecret.trim() : "";
        String expected = "Bearer " + cronSecret;
        
        if (authHeader != null && authHeader.equals(expected)) {
            // Bypass: cron worker autorizzato
            try {
                service.refreshOnlyRecentSales();
                return ResponseEntity.ok("✅ Funkard market valuations refreshed incrementally.");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        
        // 🔐 Richiede autenticazione ADMIN per utenti normali
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() ||
            auth.getAuthorities().stream().noneMatch(a -> 
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            throw new RuntimeException("Access Denied");
        }
        
        try {
            service.refreshOnlyRecentSales();
            return ResponseEntity.ok("✅ Funkard market valuations refreshed incrementally.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    public record MarketValuationRequest(
            String itemName,
            String setName,
            String category,
            String condition,
            Double grade
    ) {}
}
