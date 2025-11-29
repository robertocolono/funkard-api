package com.funkard.currency;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/public/currency")
@CrossOrigin(origins = "*")
public class CurrencyRatePublicTestController {

    @PostMapping("/test")
    public ResponseEntity<?> test() {
        System.out.println("🔥 PUBLIC TEST HIT OK");
        return ResponseEntity.ok(
            Map.of("success", true, "message", "PUBLIC TEST OK")
        );
    }
}

