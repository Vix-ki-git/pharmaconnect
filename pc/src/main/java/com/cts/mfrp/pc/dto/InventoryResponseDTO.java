package com.cts.mfrp.pc.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class InventoryResponseDTO {
    private String stockId;
    private String medicineId;
    private String medicineName;
    private String genericName;
    private String manufacturer;
    private Integer quantity;
    private Double price;
    private LocalDateTime lastUpdated;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
}