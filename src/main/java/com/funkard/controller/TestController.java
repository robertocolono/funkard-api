package com.funkard.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "message", "Funkard API is running",
            "timestamp", java.time.Instant.now().toString()
        ));
    }

    @GetMapping("/sse-test")
    public ResponseEntity<Map<String, String>> sseTest() {
        return ResponseEntity.ok(Map.of(
            "sse_endpoint", "/api/admin/notifications/stream",
            "test_endpoint", "/api/admin/notifications/test",
            "status", "SSE endpoints available"
        ));
    }
}
