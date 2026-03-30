package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.UserDTO;
import com.cts.mfrp.pc.dto.ForgotPasswordRequest; // Add this import
import com.cts.mfrp.pc.dto.ResetPasswordRequest;    // Add this import
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register/buyer")
    public ResponseEntity<User> registerBuyer(@RequestBody UserDTO userDTO) {
        User registeredUser = userService.registerBuyer(userDTO);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    // 1. New endpoint to request a reset token
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String token = userService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok("Reset token generated: " + token);
    }

    // 2. New endpoint to actually change the password using the token
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password updated successfully!");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String userId) {
        userService.logout(userId);
        return ResponseEntity.ok("User logged out successfully. Active session ended.");
    }
}