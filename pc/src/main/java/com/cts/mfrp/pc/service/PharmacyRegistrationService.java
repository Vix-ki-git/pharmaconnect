package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.SellerPharmacyRegistrationRequestDto;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.repository.PharmacyRepository;
import com.cts.mfrp.pc.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PharmacyRegistrationService {

    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;

    public PharmacyRegistrationService(PharmacyRepository pharmacyRepository, UserRepository userRepository) {
        this.pharmacyRepository = pharmacyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Pharmacy registerNewPharmacyForSeller(SellerPharmacyRegistrationRequestDto registrationRequestDto) {

        // 1. Locate the existing user who is trying to become a seller
        User prospectiveSeller = userRepository.findByEmail(registrationRequestDto.getSellerEmailAddress())
                .orElseThrow(() -> new RuntimeException("User account not found for email: " + registrationRequestDto.getSellerEmailAddress()));

        // 2. Upgrade the user's role from BUYER to SELLER
        prospectiveSeller.setRole("SELLER");
        userRepository.save(prospectiveSeller);

        // 3. Map the incoming DTO to the Database Entity
        Pharmacy newPharmacyEntity = new Pharmacy();
        newPharmacyEntity.setName(registrationRequestDto.getPharmacyName());
        newPharmacyEntity.setAddress(registrationRequestDto.getPharmacyAddress());
        newPharmacyEntity.setLat(registrationRequestDto.getLocationLatitude());
        newPharmacyEntity.setLng(registrationRequestDto.getLocationLongitude());
        newPharmacyEntity.setPhone(registrationRequestDto.getContactPhoneNumber());

        // Handle potential nulls from the frontend gracefully
        newPharmacyEntity.setIs247(registrationRequestDto.getIsOperated247() != null ? registrationRequestDto.getIsOperated247() : false);

        // 4. Apply strict Business Rules for new registrations
        newPharmacyEntity.setIsVerified(false); // Requires manual Admin approval later
        newPharmacyEntity.setIsActive(true);    // The record is active in the system
        newPharmacyEntity.setOwner(prospectiveSeller); // Link the Pharmacy to the User

        return pharmacyRepository.save(newPharmacyEntity);
    }
}