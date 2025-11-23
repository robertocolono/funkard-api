package com.funkard.controller;

import com.funkard.dto.LoginResponse;
import com.funkard.model.User;
import com.funkard.repository.UserRepository;
import com.funkard.security.JwtUtil;
import com.funkard.payload.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
}, allowCredentials = "true")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // REGISTRAZIONE
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // Validazione campi obbligatori base
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Campi obbligatori mancanti");
        }

        // 🔒 GDPR Compliance: Validazione accettazione Termini e Privacy Policy
        if (request.getAcceptTerms() == null || request.getAcceptPrivacy() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Per completare la registrazione devi accettare Termini e Privacy Policy.");
        }

        if (!request.getAcceptTerms() || !request.getAcceptPrivacy()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Per completare la registrazione devi accettare Termini e Privacy Policy.");
        }

        if (userRepository.findByEmail(request.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email già registrata");
        }

        // Crea nuovo utente con preferredCurrency, language e GDPR compliance
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.setPreferredCurrency(request.getPreferredCurrency() != null ? request.getPreferredCurrency() : "EUR");
        user.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
        user.setVerified(true);
        user.setRole("USER");
        
        // 🔒 GDPR Compliance: Salva timestamp accettazione
        user.setTermsAcceptedAt(java.time.LocalDateTime.now());
        user.setPrivacyAcceptedAt(java.time.LocalDateTime.now());
        
        // Retrocompatibilità: aggiorna anche il campo legacy
        user.setAccettaTermini(true);
        
        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getEmail());
        LoginResponse response = new LoginResponse(
            token,
            savedUser.getLanguage() != null ? savedUser.getLanguage() : "en",
            savedUser.getPreferredCurrency() != null ? savedUser.getPreferredCurrency() : "EUR"
        );
        return ResponseEntity.ok(response);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali mancanti");
        }

        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non trovato");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Password errata");
        }

        // Aggiorna ultimo accesso
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        LoginResponse response = new LoginResponse(
            token,
            user.getLanguage() != null ? user.getLanguage() : "en",
            user.getPreferredCurrency() != null ? user.getPreferredCurrency() : "EUR"
        );
        return ResponseEntity.ok(response);
    }

    // VALIDATE TOKEN (opzionale)
    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestParam String token) {
        try {
            String email = jwtUtil.getEmailFromToken(token);
            return ResponseEntity.ok("Token valido per " + email);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido");
        }
    }
}
