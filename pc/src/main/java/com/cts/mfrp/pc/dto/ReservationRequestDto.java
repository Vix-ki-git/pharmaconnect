package com.cts.mfrp.pc.dto;

import lombok.Data;

@Data
public class ReservationRequestDto {
    private String userId;
    private String pharmacyId;
    private String medicineId;
    private Integer quantity;
}
