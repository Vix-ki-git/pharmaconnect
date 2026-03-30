package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.UserDTO;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User registerBuyer(UserDTO userDTO) {
        User user = new User();

        // Mapping based on your User.java fields
        user.setName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhoneNumber());
        user.setPasswordHash(userDTO.getPassword()); // Maps to password_hash column
        user.setLat(userDTO.getLatitude());
        user.setLng(userDTO.getLongitude());
        user.setRole("BUYER");

        // id is UUID generated, createdAt is handled by @CreationTimestamp
        return userRepository.save(user);
    }

    @Override
    public String initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = java.util.UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setTokenExpiry(java.time.LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);
        return token;
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .filter(u -> u.getTokenExpiry().isAfter(java.time.LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        user.setPasswordHash(newPassword);
        user.setResetToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
    }
}