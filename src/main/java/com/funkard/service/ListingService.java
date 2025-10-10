package com.funkard.service;

import com.funkard.model.Listing;
import com.funkard.repository.ListingRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListingService {
    private final ListingRepository repo;

    public ListingService(ListingRepository repo) {
        this.repo = repo;
    }

    public List<Listing> getAll() {
        return repo.findAll();
    }

    public Listing create(Listing listing) {
        return repo.save(listing);
    }
}