package com.cts.mfrp.pc.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReservationResponseDto {
    private String id;
    private String status;
    private Integer quantity;
    private LocalDateTime holdAt;
    private LocalDateTime expiresAt;
    private String medicineName;
    private String pharmacyName;
    private String pharmacyId;
    private String medicineId;
    private String userId;
}
