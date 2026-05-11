package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.ReservationRequestDto;
import com.cts.mfrp.pc.dto.ReservationResponseDto;
import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.model.Reservation;
import com.cts.mfrp.pc.repository.PharmacyStockRepository;
import com.cts.mfrp.pc.repository.ReservationRepository;
import com.cts.mfrp.pc.repository.UserRepository;
import com.cts.mfrp.pc.repository.PharmacyRepository;
import com.cts.mfrp.pc.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PharmacyStockRepository stockRepository;
    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;
    private final DemandAnalyticsService demandAnalyticsService;

    @Transactional
    public ReservationResponseDto createReservation(ReservationRequestDto request) {
        List<PharmacyStock> stocks = stockRepository
                .findAllByPharmacyIdAndMedicineId(request.getPharmacyId(), request.getMedicineId());

        if (stocks.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not stocked at this pharmacy");
        }

        var stock = stocks.stream()
                .filter(s -> s.getQuantity() >= request.getQuantity())
                .min(Comparator.comparing(PharmacyStock::getExpiryDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow(() -> {
                    int totalAvailable = stocks.stream().mapToInt(PharmacyStock::getQuantity).sum();
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Insufficient stock in any single batch. Total available: " + totalAvailable);
                });

        var user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        stock.setQuantity(stock.getQuantity() - request.getQuantity());
        stockRepository.save(stock);

        var reservation = new Reservation();
        reservation.setUser(user);
        reservation.setPharmacy(stock.getPharmacy());
        reservation.setMedicine(stock.getMedicine());
        reservation.setQuantity(request.getQuantity());
        reservation.setStatus("PENDING");
        reservation.setHoldAt(LocalDateTime.now());
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        var saved = reservationRepository.save(reservation);

        demandAnalyticsService.recordReservation(stock.getPharmacy().getId(), stock.getMedicine().getId());

        return ReservationResponseDto.builder()
                .id(saved.getId())
                .status(saved.getStatus())
                .quantity(saved.getQuantity())
                .holdAt(saved.getHoldAt())
                .expiresAt(saved.getExpiresAt())
                .medicineName(stock.getMedicine().getName())
                .pharmacyName(stock.getPharmacy().getName())
                .pharmacyId(stock.getPharmacy().getId())
                .medicineId(stock.getMedicine().getId())
                .userId(user.getId())
                .build();
    }

    @Transactional
    public ReservationResponseDto cancelReservation(String reservationId) {
        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        if (!"PENDING".equals(reservation.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only PENDING reservations can be cancelled. Current status: " + reservation.getStatus());
        }

        stockRepository.findAllByPharmacyIdAndMedicineId(
                        reservation.getPharmacy().getId(),
                        reservation.getMedicine().getId())
                .stream()
                .min(Comparator.comparing(PharmacyStock::getExpiryDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .ifPresent(stock -> {
                    stock.setQuantity(stock.getQuantity() + reservation.getQuantity());
                    stockRepository.save(stock);
                });

        reservation.setStatus("CANCELLED");
        var saved = reservationRepository.save(reservation);

        return ReservationResponseDto.builder()
                .id(saved.getId())
                .status(saved.getStatus())
                .quantity(saved.getQuantity())
                .holdAt(saved.getHoldAt())
                .expiresAt(saved.getExpiresAt())
                .medicineName(reservation.getMedicine().getName())
                .pharmacyName(reservation.getPharmacy().getName())
                .pharmacyId(reservation.getPharmacy().getId())
                .medicineId(reservation.getMedicine().getId())
                .userId(reservation.getUser().getId())
                .build();
    }

    public List<ReservationResponseDto> getUserReservations(String userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(r -> ReservationResponseDto.builder()
                        .id(r.getId())
                        .status(r.getStatus())
                        .quantity(r.getQuantity())
                        .holdAt(r.getHoldAt())
                        .expiresAt(r.getExpiresAt())
                        .medicineName(r.getMedicine().getName())
                        .pharmacyName(r.getPharmacy().getName())
                        .pharmacyId(r.getPharmacy().getId())
                        .medicineId(r.getMedicine().getId())
                        .userId(userId)
                        .build())
                .collect(Collectors.toList());
    }

    public List<ReservationResponseDto> getPharmacyReservations(String pharmacyId) {
        return reservationRepository.findByPharmacyId(pharmacyId).stream()
                .map(r -> ReservationResponseDto.builder()
                        .id(r.getId())
                        .status(r.getStatus())
                        .quantity(r.getQuantity())
                        .holdAt(r.getHoldAt())
                        .expiresAt(r.getExpiresAt())
                        .medicineName(r.getMedicine().getName())
                        .pharmacyName(r.getPharmacy().getName())
                        .pharmacyId(r.getPharmacy().getId())
                        .medicineId(r.getMedicine().getId())
                        .userId(r.getUser().getId())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponseDto claimReservation(String reservationId) {
        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        if (!"PENDING".equals(reservation.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only PENDING reservations can be claimed. Current status: " + reservation.getStatus());
        }

        reservation.setStatus("CLAIMED");
        var saved = reservationRepository.save(reservation);

        return ReservationResponseDto.builder()
                .id(saved.getId())
                .status(saved.getStatus())
                .quantity(saved.getQuantity())
                .holdAt(saved.getHoldAt())
                .expiresAt(saved.getExpiresAt())
                .medicineName(reservation.getMedicine().getName())
                .pharmacyName(reservation.getPharmacy().getName())
                .pharmacyId(reservation.getPharmacy().getId())
                .medicineId(reservation.getMedicine().getId())
                .userId(reservation.getUser().getId())
                .build();
    }
}
