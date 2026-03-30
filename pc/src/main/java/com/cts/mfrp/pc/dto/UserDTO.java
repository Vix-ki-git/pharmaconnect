package com.cts.mfrp.pc.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String fullName;
    private String email;
    private String password;
    private String phoneNumber;
    private Float latitude;  // Changed to Float to match User.java
    private Float longitude; // Changed to Float to match User.java
}