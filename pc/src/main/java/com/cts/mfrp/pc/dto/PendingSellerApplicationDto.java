package com.cts.mfrp.pc.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PendingSellerApplicationDto {
    private String pharmacyId;
    private String pharmacyName;
    private String pharmacyAddress;
    private String pharmacyPhone;
    private boolean is247;
    private LocalDateTime registeredAt;

    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;

    private List<DocumentSummaryDto> documents;
    private String overallStatus;
}
