package com.cts.mfrp.petz.models.auth;

public class RegisterRequest {

    private String name;
    private String email;
    private String password;
    private String phone;
    private String role;     // USER (default) | NGO | HOSPITAL
    private String address;  // NGO / HOSPITAL only
    private String city;     // NGO / HOSPITAL only

    public RegisterRequest() {}

    public RegisterRequest(String name, String email, String password, String phone, String role) {
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.phone    = phone;
        this.role     = role;
    }

    public String getName()                    { return name; }
    public void setName(String value)          { this.name = value; }

    public String getEmail()                   { return email; }
    public void setEmail(String value)         { this.email = value; }

    public String getPassword()                { return password; }
    public void setPassword(String value)      { this.password = value; }

    public String getPhone()                   { return phone; }
    public void setPhone(String value)         { this.phone = value; }

    public String getRole()                    { return role; }
    public void setRole(String value)          { this.role = value; }

    public String getAddress()                 { return address; }
    public void setAddress(String value)       { this.address = value; }

    public String getCity()                    { return city; }
    public void setCity(String value)          { this.city = value; }
}
