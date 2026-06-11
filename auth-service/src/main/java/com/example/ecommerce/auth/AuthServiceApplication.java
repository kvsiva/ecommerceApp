package com.example.ecommerce.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @RestController
    @RequestMapping("/auth")
    static class AuthController {
        private final Map<String, UserAccount> users = new ConcurrentHashMap<>();

        @PostMapping("/register")
        AuthResponse register(@Valid @RequestBody RegisterRequest request) {
            if (users.containsKey(request.email())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
            }
            var account = new UserAccount(UUID.randomUUID().toString(), request.email(), request.password(), request.role());
            users.put(account.email(), account);
            return tokenFor(account);
        }

        @PostMapping("/login")
        AuthResponse login(@Valid @RequestBody LoginRequest request) {
            var account = users.get(request.email());
            if (account == null || !account.password().equals(request.password())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            }
            return tokenFor(account);
        }

        @GetMapping("/validate")
        TokenClaims validate(@RequestHeader("Authorization") String authorization) {
            var token = authorization.replace("Bearer ", "");
            var decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8).split(":");
            if (decoded.length < 4) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }
            return new TokenClaims(decoded[0], decoded[1], decoded[2], Instant.ofEpochSecond(Long.parseLong(decoded[3])));
        }

        private AuthResponse tokenFor(UserAccount account) {
            var expiresAt = Instant.now().plusSeconds(3600);
            var raw = "%s:%s:%s:%d".formatted(account.userId(), account.email(), account.role(), expiresAt.getEpochSecond());
            return new AuthResponse(Base64.getUrlEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8)), expiresAt);
        }
    }

    record RegisterRequest(@Email String email, @NotBlank String password, String role) {
        RegisterRequest {
            role = role == null || role.isBlank() ? "CUSTOMER" : role;
        }
    }
    record LoginRequest(@Email String email, @NotBlank String password) {}
    record AuthResponse(String accessToken, Instant expiresAt) {}
    record TokenClaims(String userId, String email, String role, Instant expiresAt) {}
    record UserAccount(String userId, String email, String password, String role) {}
}
