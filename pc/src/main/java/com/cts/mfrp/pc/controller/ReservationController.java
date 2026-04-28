package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.ReservationRequestDto;
import com.cts.mfrp.pc.dto.ReservationResponseDto;
import com.cts.mfrp.pc.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponseDto> createReservation(@RequestBody ReservationRequestDto request) {
        return ResponseEntity.ok(reservationService.createReservation(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationResponseDto>> getUserReservations(@PathVariable String userId) {
        return ResponseEntity.ok(reservationService.getUserReservations(userId));
    }

    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationResponseDto> cancelReservation(@PathVariable String reservationId) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId));
    }

    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<List<ReservationResponseDto>> getPharmacyReservations(@PathVariable String pharmacyId) {
        return ResponseEntity.ok(reservationService.getPharmacyReservations(pharmacyId));
    }

    @PatchMapping("/{reservationId}/claim")
    public ResponseEntity<ReservationResponseDto> claimReservation(@PathVariable String reservationId) {
        return ResponseEntity.ok(reservationService.claimReservation(reservationId));
    }
}
