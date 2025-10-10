package com.funkard.controller;

import com.funkard.model.Card;
import com.funkard.repository.CardRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*")
public class CardController {
    private final CardRepository repo;

    public CardController(CardRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Card> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Card create(@RequestBody Card card) {
        return repo.save(card);
    }
}
