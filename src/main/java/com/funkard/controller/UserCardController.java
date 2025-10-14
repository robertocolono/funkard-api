package com.funkard.controller;

import com.funkard.model.UserCard;
import com.funkard.repository.UserCardRepository;
import com.funkard.service.R2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usercards")
@CrossOrigin(origins = "https://funkard.vercel.app")
public class UserCardController {

    private final UserCardRepository userCardRepository;
    private final R2Service r2Service;

    public UserCardController(UserCardRepository userCardRepository, R2Service r2Service) {
        this.userCardRepository = userCardRepository;
        this.r2Service = r2Service;
    }

    // GET tutte le carte di un utente
    @GetMapping("/collection/{userId}")
    public ResponseEntity<List<UserCard>> getUserCollection(@PathVariable String userId) {
        return ResponseEntity.ok(userCardRepository.findByUserId(userId));
    }

    // GET singola carta
    @GetMapping("/{id}")
    public ResponseEntity<UserCard> getCard(@PathVariable String id) {
        Optional<UserCard> card = userCardRepository.findById(id);
        return card.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // POST nuova carta
    @PostMapping
    public ResponseEntity<UserCard> addCard(@RequestBody UserCard card) {
        UserCard saved = userCardRepository.save(card);
        return ResponseEntity.ok(saved);
    }

    // PUT aggiorna carta
    @PutMapping("/{id}")
    public ResponseEntity<UserCard> updateCard(@PathVariable String id, @RequestBody UserCard updatedCard) {
        return userCardRepository.findById(id)
                .map(card -> {
                    updatedCard.setId(id);
                    return ResponseEntity.ok(userCardRepository.save(updatedCard));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE carta
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable String id) {
        if (userCardRepository.existsById(id)) {
            userCardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // PUT multipart per aggiornare immagini RAW opzionali
    @PutMapping(value = "/{id}/raw-images", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateRawImages(
            @PathVariable String id,
            @RequestParam(required = false, name = "topLeftImage") org.springframework.web.multipart.MultipartFile topLeftImage,
            @RequestParam(required = false, name = "topRightImage") org.springframework.web.multipart.MultipartFile topRightImage,
            @RequestParam(required = false, name = "bottomLeftImage") org.springframework.web.multipart.MultipartFile bottomLeftImage,
            @RequestParam(required = false, name = "bottomRightImage") org.springframework.web.multipart.MultipartFile bottomRightImage,
            @RequestParam(required = false, name = "edgeTopImage") org.springframework.web.multipart.MultipartFile edgeTopImage,
            @RequestParam(required = false, name = "edgeBottomImage") org.springframework.web.multipart.MultipartFile edgeBottomImage,
            @RequestParam(required = false, name = "edgeLeftImage") org.springframework.web.multipart.MultipartFile edgeLeftImage,
            @RequestParam(required = false, name = "edgeRightImage") org.springframework.web.multipart.MultipartFile edgeRightImage
    ) {
        var optional = userCardRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var card = optional.get();
        try {
            boolean changed = false;
            if (topLeftImage != null && !topLeftImage.isEmpty()) {
                String url = r2Service.uploadUserCardFile(topLeftImage, id, "topLeft");
                card.setTopLeftImage(url); changed = true;
            }
            if (topRightImage != null && !topRightImage.isEmpty()) {
                String url = r2Service.uploadUserCardFile(topRightImage, id, "topRight");
                card.setTopRightImage(url); changed = true;
            }
            if (bottomLeftImage != null && !bottomLeftImage.isEmpty()) {
                String url = r2Service.uploadUserCardFile(bottomLeftImage, id, "bottomLeft");
                card.setBottomLeftImage(url); changed = true;
            }
            if (bottomRightImage != null && !bottomRightImage.isEmpty()) {
                String url = r2Service.uploadUserCardFile(bottomRightImage, id, "bottomRight");
                card.setBottomRightImage(url); changed = true;
            }
            if (edgeTopImage != null && !edgeTopImage.isEmpty()) {
                String url = r2Service.uploadUserCardFile(edgeTopImage, id, "edgeTop");
                card.setEdgeTopImage(url); changed = true;
            }
            if (edgeBottomImage != null && !edgeBottomImage.isEmpty()) {
                String url = r2Service.uploadUserCardFile(edgeBottomImage, id, "edgeBottom");
                card.setEdgeBottomImage(url); changed = true;
            }
            if (edgeLeftImage != null && !edgeLeftImage.isEmpty()) {
                String url = r2Service.uploadUserCardFile(edgeLeftImage, id, "edgeLeft");
                card.setEdgeLeftImage(url); changed = true;
            }
            if (edgeRightImage != null && !edgeRightImage.isEmpty()) {
                String url = r2Service.uploadUserCardFile(edgeRightImage, id, "edgeRight");
                card.setEdgeRightImage(url); changed = true;
            }
            if (changed) {
                userCardRepository.save(card);
            }
            return ResponseEntity.ok(card);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Errore durante l'upload delle immagini RAW");
        }
    }
}
