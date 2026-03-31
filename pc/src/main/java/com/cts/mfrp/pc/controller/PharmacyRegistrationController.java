package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.SellerPharmacyRegistrationRequestDto;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.service.PharmacyRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller-onboarding") // Makes the domain context obvious
public class PharmacyRegistrationController {

    private final PharmacyRegistrationService pharmacyRegistrationService;

    public PharmacyRegistrationController(PharmacyRegistrationService pharmacyRegistrationService) {
        this.pharmacyRegistrationService = pharmacyRegistrationService;
    }

    @PostMapping("/register-pharmacy")
    public ResponseEntity<?> registerPharmacyAndUpgradeUserRole(@RequestBody SellerPharmacyRegistrationRequestDto registrationRequestDto) {
        try {
            Pharmacy successfullyRegisteredPharmacy = pharmacyRegistrationService.registerNewPharmacyForSeller(registrationRequestDto);
            return ResponseEntity.ok(successfullyRegisteredPharmacy);
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body("Failed to complete seller onboarding: " + exception.getMessage());
        }
    }
}