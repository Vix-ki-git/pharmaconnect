package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.SellerPortalDashboardResponseDto;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.repository.PharmacyRepository;
import com.cts.mfrp.pc.repository.PharmacyStockRepository;
import com.cts.mfrp.pc.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerPortalAccessService {

    private final PharmacyRepository pharmacyRepository;
    private final PharmacyStockRepository stockRepository;
    private final ReservationRepository reservationRepository;

    public SellerPortalDashboardResponseDto fetchDashboardDataForSeller(String sellerEmailAddress) {

        Pharmacy sellerPharmacy = pharmacyRepository.findByOwner_Email(sellerEmailAddress)
                .orElseThrow(() -> new RuntimeException("No pharmacy found registered to this email: " + sellerEmailAddress));

        SellerPortalDashboardResponseDto dto = new SellerPortalDashboardResponseDto();
        dto.setPharmacyId(sellerPharmacy.getId());
        dto.setPharmacyName(sellerPharmacy.getName());
        dto.setIsPharmacyVerified(sellerPharmacy.getIsVerified());
        dto.setIsPharmacyActive(sellerPharmacy.getIsActive());

        if (Boolean.TRUE.equals(sellerPharmacy.getIsVerified())) {
            dto.setPortalAccessMessage("Welcome to your Seller Portal. You have full access.");
        } else {
            dto.setPortalAccessMessage("ACCOUNT PENDING: Your registration is currently under review by an Administrator. You cannot add inventory yet.");
        }

        // US-11: Stock summary
        List<PharmacyStock> stock = stockRepository.findByPharmacyId(sellerPharmacy.getId());
        dto.setTotalStockItems(stock.size());
        dto.setOutOfStockItems((int) stock.stream().filter(s -> s.getQuantity() == 0).count());
        dto.setLowStockItems((int) stock.stream().filter(s -> s.getQuantity() > 0 && s.getQuantity() <= 10).count());

        // US-12: Active reservations count
        List<?> activeReservations = reservationRepository.findByPharmacyId(sellerPharmacy.getId())
                .stream().filter(r -> "PENDING".equals(r.getStatus())).toList();
        dto.setActiveReservationsCount(activeReservations.size());

        return dto;
    }
}