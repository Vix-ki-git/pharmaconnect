package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.RegistrationRequest;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Value("${google.client.id}")
    private String googleClientId;
    private String idToken;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public User authenticateUser(String email, String rawPassword) {
        // 1. Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // 2. Verify password against the hash in DB
        if (passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return user;
        } else {
            // PCP-8 Error Handling
            throw new RuntimeException("Invalid password. Please try again.");
        }
    }

    public User registerNewUser(RegistrationRequest regRequest) {
        // 1. Check if email is already taken
        if (userRepository.findByEmail(regRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered!");
        }

        // 2. Create and map the User entity
        User user = new User();
        user.setName(regRequest.getName());
        user.setEmail(regRequest.getEmail());
        user.setPhone(regRequest.getPhone());
        user.setRole(regRequest.getRole());

        // 3. Encrypt the password (Best Practice!)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPasswordHash(encoder.encode(regRequest.getPassword()));

        return userRepository.save(user);
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
}