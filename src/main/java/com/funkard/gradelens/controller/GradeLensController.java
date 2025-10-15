package com.funkard.gradelens.controller;

import com.funkard.gradelens.model.GradeLensResponse;
import com.funkard.gradelens.service.GradeLensService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gradelens")
@CrossOrigin(origins = {"https://funkard.vercel.app", "https://funkardnew.vercel.app"})
public class GradeLensController {

    private final GradeLensService service;

    public GradeLensController(GradeLensService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public ResponseEntity<GradeLensResponse> analyze(@RequestBody Map<String, String> body) {
        String front = body.get("frontImageUrl");
        String back = body.get("backImageUrl");
        service.testOpenCV(); // test locale
        return ResponseEntity.ok(service.analyze(front, back));
    }
}
