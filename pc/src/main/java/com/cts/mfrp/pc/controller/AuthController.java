package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.GoogleLoginRequest;
import com.cts.mfrp.pc.dto.LoginRequest;
import com.cts.mfrp.pc.dto.RegistrationRequest;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
// 🚨 Notice: I removed @CrossOrigin("*") because your SecurityConfig handles it now!
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 🛠️ NEW: This helper method builds a secure, HttpOnly cookie
    private ResponseCookie createSessionCookie(String token) {
        return ResponseCookie.from("AUTH_SESSION", token)
                .httpOnly(true)       // Prevents XSS attacks (Angular can't read it, only the browser can)
                .secure(false)        // Keep false for localhost. Set to true in Production (HTTPS)
                .path("/")            // Cookie is valid for all API routes
                .maxAge(24 * 60 * 60) // Expires in 24 hours
                .sameSite("Lax")      // Allows safe cross-origin requests
                .build();
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogleUser(@RequestBody GoogleLoginRequest request) {
        try {
            User user = authService.verifyAndLoginWithGoogle(request.getIdToken());

            // ⚠️ TODO: Replace this placeholder with your actual JWT generation logic later
            String sessionToken = "jwt-token-for-" + user.getEmail();
            ResponseCookie authCookie = createSessionCookie(sessionToken);

            // 🛠️ NEW: We inject the cookie into the HTTP Headers before returning the user
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                    .body(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Google Auth failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            User user = authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());

            // ⚠️ TODO: Replace this placeholder with your actual JWT generation logic later
            String sessionToken = "jwt-token-for-" + user.getEmail();
            ResponseCookie authCookie = createSessionCookie(sessionToken);

            // 🛠️ NEW: We inject the cookie into the HTTP Headers before returning the user
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                    .body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest regRequest) {
        try {
            User newUser = authService.registerNewUser(regRequest);
            // Registration doesn't usually log you in instantly, so no cookie here. Just a success message!
            return ResponseEntity.status(201).body("Registration successful for: " + newUser.getEmail());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}