package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.SellerPortalDashboardResponseDto;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.repository.PharmacyRepository;
import org.springframework.stereotype.Service;

@Service
public class SellerPortalAccessService {

    private final PharmacyRepository pharmacyRepository;

    public SellerPortalAccessService(PharmacyRepository pharmacyRepository) {
        this.pharmacyRepository = pharmacyRepository;
    }

    public SellerPortalDashboardResponseDto fetchDashboardDataForSeller(String sellerEmailAddress) {

        // 1. Find the specific pharmacy belonging to this seller
        Pharmacy sellerPharmacy = pharmacyRepository.findByOwner_Email(sellerEmailAddress)
                .orElseThrow(() -> new RuntimeException("No pharmacy found registered to this email: " + sellerEmailAddress));

        // 2. Prepare the response for the frontend
        SellerPortalDashboardResponseDto dashboardResponseDto = new SellerPortalDashboardResponseDto();
        dashboardResponseDto.setPharmacyId(sellerPharmacy.getId());
        dashboardResponseDto.setPharmacyName(sellerPharmacy.getName());
        dashboardResponseDto.setIsPharmacyVerified(sellerPharmacy.getIsVerified());
        dashboardResponseDto.setIsPharmacyActive(sellerPharmacy.getIsActive());

        // 3. The "Bouncer" Logic: Check if the Admin has approved them yet
        if (Boolean.TRUE.equals(sellerPharmacy.getIsVerified())) {
            dashboardResponseDto.setPortalAccessMessage("Welcome to your Seller Portal. You have full access.");
        } else {
            dashboardResponseDto.setPortalAccessMessage("ACCOUNT PENDING: Your registration is currently under review by an Administrator. You cannot add inventory yet.");
        }

        return dashboardResponseDto;
    }
}