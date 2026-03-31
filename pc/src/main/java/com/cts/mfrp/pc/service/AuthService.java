package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthService {

    private final UserRepository userRepository;

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User verifyAndLoginWithGoogle(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // Check if user exists, otherwise create new
            return userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setRole("BUYER");
                // Google users don't have a local password, we can set a dummy or leave null
                newUser.setPasswordHash("OAUTH_USER");
                return userRepository.save(newUser);
            });
        } else {
            throw new RuntimeException("Invalid ID token.");
        }
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