package com.cts.mfrp.pc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUploadDTO {
    private String medicineName;
    private Integer quantity;
    private Double price;
    private String manufacturingDate; // Expecting "YYYY-MM-DD"
    private String expiryDate;        // Expecting "YYYY-MM-DD"
}
