package com.funkard.controller;

import com.funkard.model.Wishlist;
import com.funkard.repository.WishlistRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
public class WishlistController {
    private final WishlistRepository repo;

    public WishlistController(WishlistRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Wishlist> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Wishlist create(@RequestBody Wishlist wishlist) {
        return repo.save(wishlist);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        repo.deleteById(id);
    }
}
