package com.cts.mfrp.pc.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistrationRequest {

    @NotBlank(message = "Full name is required")
    private String name;

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;


}