package com.funkard.controller;

import com.funkard.model.User;
import com.funkard.model.VerificationToken;
import com.funkard.repository.UserRepository;
import com.funkard.repository.VerificationTokenRepository;
import com.funkard.security.JwtUtil;
import com.funkard.service.EmailService;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final UserRepository repo;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final JwtUtil jwt;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository repo, VerificationTokenRepository verificationTokenRepository, 
                         EmailService emailService, JwtUtil jwt) {
        this.repo = repo;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
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
        var optionalToken = verificationTokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token non valido"));
        }

        VerificationToken vt = optionalToken.get();
        if (vt.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token scaduto"));
        }

        User user = vt.getUser();
        user.setVerified(true);
        repo.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Account verificato con successo!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User existing = repo.findByEmail(user.getEmail());
        if (existing != null && passwordEncoder.matches(user.getPassword(), existing.getPassword())) {
            if (!existing.getVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Account non verificato. Controlla la tua email."));
            }
            String token = jwt.generateToken(user.getEmail());
            return ResponseEntity.ok(Map.of("token", token, "email", user.getEmail()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
