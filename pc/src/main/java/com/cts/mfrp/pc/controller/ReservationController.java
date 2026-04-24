package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.ReservationRequestDto;
import com.cts.mfrp.pc.dto.ReservationResponseDto;
import com.cts.mfrp.pc.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
