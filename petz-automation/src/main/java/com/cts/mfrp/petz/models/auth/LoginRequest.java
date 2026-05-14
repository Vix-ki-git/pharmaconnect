package com.cts.mfrp.petz.models.auth;

public class LoginRequest {

    private String email;
    private String password;

    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail()                   { return email; }
    public void setEmail(String value)         { this.email = value; }

    public String getPassword()                { return password; }
    public void setPassword(String value)      { this.password = value; }
}
