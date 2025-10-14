package com.funkard.controller;

import com.funkard.model.UserCard;
import com.funkard.repository.UserCardRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "https://funkard.vercel.app")
public class UserCardController {

    private final UserCardRepository userCardRepository;

    public UserCardController(UserCardRepository userCardRepository) {
        this.userCardRepository = userCardRepository;
    }

    // GET tutte le carte di un utente
    @GetMapping("/collection/{userId}")
    public ResponseEntity<List<UserCard>> getUserCollection(@PathVariable String userId) {
        return ResponseEntity.ok(userCardRepository.findByUserId(userId));
    }

    // GET singola carta
    @GetMapping("/usercards/{id}")
    public ResponseEntity<UserCard> getCard(@PathVariable String id) {
        Optional<UserCard> card = userCardRepository.findById(id);
        return card.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // POST nuova carta
    @PostMapping("/usercards")
    public ResponseEntity<UserCard> addCard(@RequestBody UserCard card) {
        UserCard saved = userCardRepository.save(card);
        return ResponseEntity.ok(saved);
    }

    // PUT aggiorna carta
    @PutMapping("/usercards/{id}")
    public ResponseEntity<UserCard> updateCard(@PathVariable String id, @RequestBody UserCard updatedCard) {
        return userCardRepository.findById(id)
                .map(card -> {
                    updatedCard.setId(id);
                    return ResponseEntity.ok(userCardRepository.save(updatedCard));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE carta
    @DeleteMapping("/usercards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable String id) {
        if (userCardRepository.existsById(id)) {
            userCardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
