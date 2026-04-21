package com.cts.mfrp.pc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceComparisonDTO {
    private String pharmacyName;
    private String address;
    private Double price;
    private Integer quantity;
    private Boolean is247;
}