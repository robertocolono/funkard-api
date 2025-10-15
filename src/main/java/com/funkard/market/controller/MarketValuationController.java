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

    public record MarketValuationRequest(
            String itemName,
            String setName,
            String category,
            String condition,
            Double grade
    ) {}
}
