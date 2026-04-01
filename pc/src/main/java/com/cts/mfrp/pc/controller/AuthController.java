package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.GoogleLoginRequest;
import com.cts.mfrp.pc.dto.LoginRequest;
import com.cts.mfrp.pc.dto.RegistrationRequest;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private ResponseCookie createSessionCookie(String token) {
        return ResponseCookie.from("AUTH_SESSION", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogleUser(@RequestBody GoogleLoginRequest request) {
        try {
            User user = authService.verifyAndLoginWithGoogle(request.getIdToken());
            String sessionToken = "jwt-token-for-" + user.getEmail();
            ResponseCookie authCookie = createSessionCookie(sessionToken);
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, authCookie.toString()).body(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Google Auth failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            User user = authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
            String sessionToken = "jwt-token-for-" + user.getEmail();
            ResponseCookie authCookie = createSessionCookie(sessionToken);
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, authCookie.toString()).body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest regRequest) {
        try {
            User newUser = authService.registerNewUser(regRequest);
            return ResponseEntity.status(201).body("Registration successful for: " + newUser.getEmail());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            authService.processForgotPassword(email);
        } catch (Exception e) {
            // Log the error but don't tell the user the email doesn't exist for security
            System.out.println("Forgot password attempt for non-existent or failed email: " + email);
        }
        return ResponseEntity.ok(Map.of("message", "If an account exists with this email, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            authService.resetPassword(request.get("token"), request.get("newPassword"));
            return ResponseEntity.ok("Password reset successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}