package com.cts.mfrp.pc.dto;

import lombok.Data;

@Data
public class MedicineRequestDto {
    private String name;
    private String genericName;
    private String category;
    private String manufacturer;
    private String dosageForm;
    private String strength;
}
