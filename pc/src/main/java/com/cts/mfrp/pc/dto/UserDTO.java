package com.cts.mfrp.pc.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String fullName;
    private String email;
    private String password;
    private String phoneNumber;
    private Float latitude;
    private Float longitude;
}