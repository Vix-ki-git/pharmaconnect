package com.cts.mfrp.pc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SellerPharmacyRegistrationRequestDto {
    private String pharmacyName;
    private String pharmacyAddress;
    private Float locationLatitude;
    private Float locationLongitude;
    private String contactPhoneNumber;
    private Boolean isOperated247;

    @NotBlank(message = "Seller email is required")
    @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
           message = "Please provide a valid email address (e.g. contact@pharmacy.com)")
    private String sellerEmailAddress; // Explicitly clarifies whose email this is
}