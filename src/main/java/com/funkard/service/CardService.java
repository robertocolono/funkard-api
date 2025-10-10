package com.funkard.service;

import com.funkard.model.Card;
import com.funkard.repository.CardRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CardService {
    private final CardRepository repo;

    public CardService(CardRepository repo) {
        this.repo = repo;
    }

    public List<Card> getAll() {
        return repo.findAll();
    }

    public Card create(Card card) {
        return repo.save(card);
    }
}