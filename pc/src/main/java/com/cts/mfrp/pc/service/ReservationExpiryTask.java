package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.model.Reservation;
import com.cts.mfrp.pc.repository.PharmacyStockRepository;
import com.cts.mfrp.pc.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReservationExpiryTask {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PharmacyStockRepository stockRepository;

    // Runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void expireOldReservations() {
        // Find reservations that are "PENDING" and past their expiresAt time
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndExpiresAtBefore("PENDING", now);

        for (Reservation res : expiredReservations) {
            // Use the Pharmacy and Medicine from the reservation to find the Stock record
            stockRepository.findByPharmacyIdAndMedicineId(
                            res.getPharmacy().getId(),
                            res.getMedicine().getId())
                    .ifPresent(stock -> {
                        // Restore the quantity
                        stock.setQuantity(stock.getQuantity() + res.getQuantity());
                        stockRepository.save(stock);
                    });

            // Update reservation status
            res.setStatus("EXPIRED");
            reservationRepository.save(res);

            System.out.println("Cron Job: Reservation " + res.getId() + " expired. Stock restored.");
        }
    }
}