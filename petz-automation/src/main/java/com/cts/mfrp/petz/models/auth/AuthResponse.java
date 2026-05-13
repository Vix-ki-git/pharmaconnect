package com.cts.mfrp.petz.models.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {

    private String  token;
    private Long    userId;
    private String  email;
    private String  name;
    private String  role;
    private Boolean isApproved;

    public String  getToken()                  { return token; }
    public void setToken(String value)         { this.token = value; }

    public Long    getUserId()                 { return userId; }
    public void setUserId(Long value)          { this.userId = value; }

    public String  getEmail()                  { return email; }
    public void setEmail(String value)         { this.email = value; }

    public String  getName()                   { return name; }
    public void setName(String value)          { this.name = value; }

    public String  getRole()                   { return role; }
    public void setRole(String value)          { this.role = value; }

    public Boolean getIsApproved()             { return isApproved; }
    public void setIsApproved(Boolean value)   { this.isApproved = value; }
}
