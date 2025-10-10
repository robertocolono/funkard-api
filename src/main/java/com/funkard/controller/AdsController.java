package com.funkard.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ads")
@CrossOrigin(origins = "*")
public class AdsController {

    @GetMapping("/gradelens")
    public Map<String, Object> gradelensBanner() {
        return Map.of(
            "placement", "gradelens_result_bottom",
            "type", "sponsorship",
            "title", "Offerta del giorno: sleeve & toploader bundle",
            "ctaText", "Scopri l'offerta",
            "ctaUrl", "https://partner.example.com/bundle-protezione",
            "imageUrl", "https://cdn.example.com/ads/sleeves.jpg"
        );
    }
}