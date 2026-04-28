package com.cts.mfrp.pc.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DemandAnalyticsResponseDto {
    private String medicineId;
    private String medicineName;
    private String genericName;
    private Integer searchCount;
    private Integer reservationCount;
    private LocalDate periodDate;
}
