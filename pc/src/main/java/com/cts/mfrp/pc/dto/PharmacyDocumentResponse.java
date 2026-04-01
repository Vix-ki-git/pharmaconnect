package com.cts.mfrp.pc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyDocumentResponse {
    private String id;
    private String pharmacyId;
    private String documentType;
    private String status;
    private LocalDateTime uploadedAt;
    private String message;
}