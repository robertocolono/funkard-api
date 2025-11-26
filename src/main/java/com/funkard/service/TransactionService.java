package com.funkard.service;

import com.funkard.model.Transaction;
import com.funkard.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository repo;

    public TransactionService(TransactionRepository repo) {
        this.repo = repo;
    }

    public List<Transaction> getAll() {
        return repo.findAll();
    }

    public Transaction create(Transaction t) {
        // 💱 Valida e imposta currency (default USD se non fornita)
        if (t.getCurrency() == null || t.getCurrency().trim().isEmpty()) {
            t.setCurrency("USD");
        } else {
            String currency = t.getCurrency().trim().toUpperCase();
            if (!com.funkard.config.SupportedCurrencies.isValid(currency)) {
                throw new IllegalArgumentException("Valuta non supportata: " + currency + 
                    ". Valute supportate: EUR, USD, GBP, JPY, BRL, CAD, AUD");
            }
            t.setCurrency(currency);
        }
        
        return repo.save(t);
    }
}