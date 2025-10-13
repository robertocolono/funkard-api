package com.funkard.controller;

import com.funkard.model.User;
import com.funkard.repository.UserRepository;
import com.funkard.security.JwtUtil;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final UserRepository repo;
    private final JwtUtil jwt;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository repo, JwtUtil jwt) {
        this.repo = repo;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User userRequest) {
        // Validazione campi obbligatori
        if (userRequest.getEmail() == null || userRequest.getPassword() == null ||
            userRequest.getHandle() == null || userRequest.getNome() == null ||
            userRequest.getPaese() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Campi obbligatori mancanti"));
        }

        // Validazione accettazione termini
        if (userRequest.getAccettaTermini() == null || !userRequest.getAccettaTermini()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Devi accettare i Termini e Condizioni"));
        }

        // Controllo duplicati
        if (repo.existsByEmail(userRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email già registrata"));
        }

        if (repo.existsByHandle(userRequest.getHandle())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Handle già in uso"));
        }

        // Hash password
        userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        userRequest.setVerified(false);

        try {
            repo.save(userRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Registrazione completata con successo"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User existing = repo.findByEmail(user.getEmail());
        if (existing != null && passwordEncoder.matches(user.getPassword(), existing.getPassword())) {
            String token = jwt.generateToken(user.getEmail());
            return ResponseEntity.ok(Map.of("token", token, "email", user.getEmail()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
