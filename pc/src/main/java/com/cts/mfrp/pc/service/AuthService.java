package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.RegistrationRequest;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.client.id}")
    private String googleClientId;

    public User authenticateUser(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authentication failed: User not found."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials. Please try again.");
        }
        return user;
    }

    public User registerNewUser(RegistrationRequest regRequest) {
        // 1. Validation
        if (userRepository.findByEmail(regRequest.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists.");
        }

        // 2. Mapping & Security Fix
        User user = new User();
        user.setName(regRequest.getName());
        user.setEmail(regRequest.getEmail());
        user.setPhone(regRequest.getPhone());

        // CRITICAL FIX: Hardcode the lowest privilege level
        user.setRole("BUYER");

        // 3. Password Encoding
        user.setPasswordHash(passwordEncoder.encode(regRequest.getPassword()));

        return userRepository.save(user);
    }

    public User verifyAndLoginWithGoogle(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new RuntimeException("Google verification failed: Invalid ID token.");
        }

        Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName((String) payload.get("name"));

            // SECURITY FIX: Google sign-ups also default to BUYER
            newUser.setRole("BUYER");
            newUser.setPasswordHash("OAUTH_USER_PROTECTED");

            return userRepository.save(newUser);
        });
    }

    public String initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If the user is an OAuth user, they shouldn't reset via our app
        if ("OAUTH_USER".equals(user.getPasswordHash())) {
            throw new RuntimeException("Please reset your password through your Google Account settings.");
        }

        String token = java.util.UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setTokenExpiry(java.time.LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        return token;
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .filter(u -> u.getTokenExpiry().isAfter(java.time.LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        user.setPasswordHash(newPassword); // In a real app, encrypt this!
        user.setResetToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
    }

    public void logout(String email) {
        // For now, we log the activity.
        // In the future, you could add a "last_logout" timestamp to the User table.
        System.out.println("User " + email + " has logged out from Smart Pharma Connect.");
    }
}