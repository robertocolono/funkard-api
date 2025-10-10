package com.funkard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
    
    @GetMapping("/")
    public String home() {
        return "Funkard API is running ✅";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}