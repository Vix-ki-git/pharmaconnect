package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.GoogleLoginRequest;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allows your frontend to talk to this backend
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
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody java.util.Map<String, String> request) {
        String token = authService.initiatePasswordReset(request.get("email"));
        return ResponseEntity.ok("Reset token generated: " + token);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String email) {
        authService.logout(email);
        return ResponseEntity.ok("Successfully logged out.");
    }
}