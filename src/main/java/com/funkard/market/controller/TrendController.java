package com.funkard.market.controller;

import com.funkard.market.service.TrendService;
import com.funkard.market.trend.TrendDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trends")
@CrossOrigin(origins = {"https://funkard.vercel.app", "https://funkard.com"})
public class TrendController {
    
    private final TrendService service;
    
    public TrendController(TrendService service) {
        this.service = service;
    }
    
    @GetMapping("/{rangeType}/{itemName}")
    public ResponseEntity<TrendDTO> getTrend(
            @PathVariable String rangeType,
            @PathVariable String itemName,
            @RequestParam(defaultValue = "card") String category
    ) {
        var dto = service.getTrendDTO(itemName, category, rangeType);
        return ResponseEntity.ok(dto);
    }
}
