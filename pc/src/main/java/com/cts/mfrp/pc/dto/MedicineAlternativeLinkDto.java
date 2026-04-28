package com.cts.mfrp.pc.dto;

import lombok.Data;

@Data
public class MedicineAlternativeLinkDto {
    private String medicineId;
    private String alternativeId;
    private String equivalenceNote;
}
