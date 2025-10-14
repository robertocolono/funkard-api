package com.funkard.controller;

import com.funkard.model.Card;
import com.funkard.repository.CardRepository;
import com.funkard.service.R2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/collection")
@CrossOrigin(origins = "*")
public class CollectionController {

    private final CardRepository cardRepository;
    private final R2Service r2Service;

    public CollectionController(CardRepository cardRepository, R2Service r2Service) {
        this.cardRepository = cardRepository;
        this.r2Service = r2Service;
    }

    // Crea una card nella collezione, opzionalmente caricando un'immagine
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Card> create(
            @RequestPart("card") Card card,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        if (file != null && !file.isEmpty()) {
            String key = r2Service.uploadFile(file, "collection");
            String publicBase = System.getenv("R2_PUBLIC_BASE_URL");
            String url = (publicBase != null && !publicBase.isBlank()) ? (publicBase.endsWith("/") ? publicBase + key : publicBase + "/" + key) : key;
            card.setImageUrl(url);
            card.setSource("collection");
        }
        Card saved = cardRepository.save(card);
        return ResponseEntity.ok(saved);
    }
}
