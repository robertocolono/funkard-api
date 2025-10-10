package com.funkard.controller;

import com.funkard.model.Listing;
import com.funkard.service.ListingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@CrossOrigin(origins = "*")
public class ListingController {

    private final ListingService service;

    public ListingController(ListingService service) {
        this.service = service;
    }

    @GetMapping
    public List<Listing> getAllListings() {
        return service.getAll();
    }

    @PostMapping
    public Listing create(@RequestBody Listing listing) {
        return service.create(listing);
    }
}
