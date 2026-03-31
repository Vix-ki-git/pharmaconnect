package com.cts.mfrp.pc.dto;

import lombok.Data;

@Data
public class SellerPharmacyRegistrationRequestDto {
    private String pharmacyName;
    private String pharmacyAddress;
    private Float locationLatitude;
    private Float locationLongitude;
    private String contactPhoneNumber;
    private Boolean isOperated247;
    private String sellerEmailAddress; // Explicitly clarifies whose email this is
}