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

        User prospectiveSeller = userRepository.findByEmail(registrationRequestDto.getSellerEmailAddress())
                .orElseThrow(() -> new RuntimeException("User account not found for email: " + registrationRequestDto.getSellerEmailAddress()));

        prospectiveSeller.setRole("SELLER");
        userRepository.save(prospectiveSeller);

        Pharmacy newPharmacyEntity = new Pharmacy();
        newPharmacyEntity.setName(registrationRequestDto.getPharmacyName());
        newPharmacyEntity.setAddress(registrationRequestDto.getPharmacyAddress());
        newPharmacyEntity.setLat(registrationRequestDto.getLocationLatitude());
        newPharmacyEntity.setLng(registrationRequestDto.getLocationLongitude());
        newPharmacyEntity.setPhone(registrationRequestDto.getContactPhoneNumber());

        newPharmacyEntity.setIs247(registrationRequestDto.getIsOperated247() != null ? registrationRequestDto.getIsOperated247() : false);

        newPharmacyEntity.setIsVerified(false);
        newPharmacyEntity.setIsActive(true);
        newPharmacyEntity.setOwner(prospectiveSeller);

        return pharmacyRepository.save(newPharmacyEntity);
    }
}