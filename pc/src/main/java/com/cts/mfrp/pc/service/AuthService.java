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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // 🛠️ Added this

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
        if (userRepository.findByEmail(regRequest.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists.");
        }
        User user = new User();
        user.setName(regRequest.getName());
        user.setEmail(regRequest.getEmail());
        user.setPhone(regRequest.getPhone());
        user.setRole("BUYER");
        user.setPasswordHash(passwordEncoder.encode(regRequest.getPassword()));
        return userRepository.save(user);
    }

    public User verifyAndLoginWithGoogle(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) throw new RuntimeException("Google verification failed.");

        Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName((String) payload.get("name"));
            newUser.setRole("BUYER");
            newUser.setPasswordHash("OAUTH_USER_PROTECTED");
            return userRepository.save(newUser);
        });
    }

    @Transactional
    public String processForgotPassword(String email) {
        // 1. Find User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Error: This email address is not registered in our system."));

        // 2. Check if OAuth user (cannot reset password here)
        if ("OAUTH_USER_PROTECTED".equals(user.getPasswordHash())) {
            throw new RuntimeException("Please use Google Login to access your account.");
        }

        // 3. Generate Token
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(15));

        // 4. Save to DB
        userRepository.save(user);

        // 5. Trigger Email
        emailService.sendResetPasswordEmail(user.getEmail(), token);

        return "A password reset link has been sent to your email.";
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token."));

        if (user.getTokenExpiry() == null || user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("The reset link has expired.");
        }

        // 🛠️ Encrypt the new password!
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
    }

    public void logout(String email) {
        System.out.println("User " + email + " has logged out.");
    }
}