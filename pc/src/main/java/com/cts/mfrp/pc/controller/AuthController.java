package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.GoogleLoginRequest;
import com.cts.mfrp.pc.dto.LoginRequest; // Your DTO for PCP-7
import com.cts.mfrp.pc.dto.RegistrationRequest;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.service.AuthService;
import jakarta.validation.Valid; // Triggers PCP-7 Validation
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogleUser(@RequestBody GoogleLoginRequest request) {
        try {
            User user = authService.verifyAndLoginWithGoogle(request.getIdToken());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Google Auth failed: " + e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Calls your custom authentication logic
            User user = authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            // Standard error handling for PCP-8
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
    // --- PCP-7: Registration Validation & Processing ---
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest regRequest) {
        try {
            // We pass the validated DTO to the service
            User newUser = authService.registerNewUser(regRequest);
            return ResponseEntity.status(201).body("Registration successful for: " + newUser.getEmail());
        } catch (RuntimeException e) {
            // Handle cases like "Email already exists"
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}