package com.cts.mfrp.pc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
           message = "Please provide a valid email address (e.g. john@example.com)")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}