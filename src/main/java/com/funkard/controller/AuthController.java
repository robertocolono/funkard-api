package com.funkard.controller;

import com.funkard.model.User;
import com.funkard.model.VerificationToken;
import com.funkard.repository.UserRepository;
import com.funkard.repository.VerificationTokenRepository;
import com.funkard.security.JwtUtil;
import com.funkard.service.EmailService;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "https://funkardnew.vercel.app")
public class AuthController {
    private final UserRepository repo;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final JwtUtil jwt;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository repo, VerificationTokenRepository verificationTokenRepository,
                          EmailService emailService, JwtUtil jwt, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
        this.jwt = jwt;
        this.passwordEncoder = passwordEncoder;
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
            User savedUser = repo.save(userRequest);

            // Genera token e salva
            VerificationToken token = VerificationToken.generate(savedUser);
            verificationTokenRepository.save(token);

            // Invia mail
            emailService.sendVerificationEmail(savedUser.getEmail(), token.getToken());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Registrazione completata. Controlla la tua email per verificare l'account."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam("token") String token) {
        // 1️⃣ Pulizia lazy dei token scaduti
        try {
            verificationTokenRepository.deleteAllByExpiryDateBefore(LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Errore durante la pulizia dei token scaduti: " + e.getMessage());
        }

        // 2️⃣ Verifica token attuale
        var optionalToken = verificationTokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token non valido"));
        }

        VerificationToken vt = optionalToken.get();
        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(vt);
            return ResponseEntity.badRequest().body(Map.of("error", "Token scaduto"));
        }

        // 3️⃣ Verifica completata
        User user = vt.getUser();
        if (user.getVerified() != null && user.getVerified()) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Account già verificato."));
        }

        user.setVerified(true);
        repo.save(user);
        verificationTokenRepository.delete(vt); // rimuove il token usato

        return ResponseEntity.ok(Map.of("success", true, "message", "✅ Account verificato con successo!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        // 🧹 Pulizia automatica dei token scaduti anche al login
        try {
            verificationTokenRepository.deleteAllByExpiryDateBefore(LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Errore durante la pulizia dei token scaduti: " + e.getMessage());
        }

        String email = credentials.get("email");
        String password = credentials.get("password");

        User existing = repo.findByEmail(email);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Utente non trovato"));
        }

        if (!passwordEncoder.matches(password, existing.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Password errata"));
        }

        if (!Boolean.TRUE.equals(existing.getVerified())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Account non verificato. Controlla la tua email."));
        }

        String token = jwt.generateToken(email);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login riuscito");
        response.put("token", token);
        response.put("userId", existing.getId());
        response.put("username", existing.getUsername());

        return ResponseEntity.ok(response);
    }
}
