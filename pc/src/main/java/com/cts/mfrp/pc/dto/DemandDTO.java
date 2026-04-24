package com.cts.mfrp.pc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandDTO {
    private String medicineName;
    private Long searchCount;
}