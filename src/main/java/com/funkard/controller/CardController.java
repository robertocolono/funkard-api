package com.funkard.controller;

import com.funkard.model.Card;
import com.funkard.service.CardService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*")
public class CardController {
    private final CardService service;

    public CardController(CardService service) {
        this.service = service;
    }

    @GetMapping
    public List<Card> getAll() {
        return service.getAll();
    }

    @PostMapping
    public Card create(@RequestBody Card card) {
        return service.create(card);
    }
}
