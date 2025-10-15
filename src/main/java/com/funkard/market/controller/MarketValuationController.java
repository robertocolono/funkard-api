package com.funkard.market.controller;

import com.funkard.market.model.MarketValuation;
import com.funkard.market.service.MarketValuationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> refreshIncremental(@RequestHeader("Authorization") String auth) {
        String expected = "Bearer " + System.getenv("FUNKARD_CRON_SECRET");
        if (!expected.equals(auth)) {
            return ResponseEntity.status(403).body("Unauthorized");
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
