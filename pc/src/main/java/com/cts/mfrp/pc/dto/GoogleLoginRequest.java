package com.cts.mfrp.pc.dto;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String idToken; // The long string received from Google on the frontend
}