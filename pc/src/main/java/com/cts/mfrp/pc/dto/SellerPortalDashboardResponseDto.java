package com.cts.mfrp.pc.dto;

import lombok.Data;

@Data
public class SellerPortalDashboardResponseDto {
    private String pharmacyId;
    private String pharmacyName;
    private Boolean isPharmacyVerified;
    private Boolean isPharmacyActive;
    private String portalAccessMessage;

    // US-11: Stock summary
    private int totalStockItems;
    private int lowStockItems;
    private int outOfStockItems;

    // US-12: Active reservations
    private long activeReservationsCount;
}