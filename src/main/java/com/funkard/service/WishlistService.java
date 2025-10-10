package com.funkard.service;

import com.funkard.model.Wishlist;
import com.funkard.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WishlistService {
    private final WishlistRepository repo;

    public WishlistService(WishlistRepository repo) {
        this.repo = repo;
    }

    public List<Wishlist> getAll() {
        return repo.findAll();
    }

    public Wishlist create(Wishlist w) {
        return repo.save(w);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }
}