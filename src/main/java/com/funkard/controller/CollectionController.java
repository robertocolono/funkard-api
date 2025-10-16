package com.funkard.controller;

import com.funkard.admin.service.AdminNotificationService;
import com.funkard.model.Card;
import com.funkard.model.UserCard;
import com.funkard.repository.CardRepository;
import com.funkard.repository.UserCardRepository;
import com.funkard.service.R2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/collection")
@CrossOrigin(origins = "*")
public class CollectionController {

    private final CardRepository cardRepository;
    private final UserCardRepository userCardRepository;
    private final R2Service r2Service;
    private final AdminNotificationService adminNotificationService;

    public CollectionController(CardRepository cardRepository, UserCardRepository userCardRepository, R2Service r2Service, AdminNotificationService adminNotificationService) {
        this.cardRepository = cardRepository;
        this.userCardRepository = userCardRepository;
        this.r2Service = r2Service;
        this.adminNotificationService = adminNotificationService;
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
        // Notifica admin per nuovo prodotto senza storico
        adminNotificationService.addNotification("new_card", "Nuova carta in collezione", "Carta: " + saved.getName(), "normal");
        return ResponseEntity.ok(saved);
    }

    // Recupera la collezione dell'utente (UserCard) per compatibilità con il frontend
    @GetMapping("/{userId}")
    public ResponseEntity<List<UserCard>> getUserCollection(@PathVariable String userId) {
        List<UserCard> cards = userCardRepository.findByUserId(userId);
        return ResponseEntity.ok(cards);
    }
}
