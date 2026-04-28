package com.cts.mfrp.pc.dto;

import lombok.Data;

@Data
public class SellerPortalDashboardResponseDto {
    private String pharmacyId;
    private String pharmacyName;
    private String pharmacyAddress;
    private String pharmacyPhone;
    private Boolean is247Open;
    private Boolean isPharmacyVerified;
    private Boolean isPharmacyActive;
    private String portalAccessMessage;
    private String ownerPhone;
    private String memberSince;

    // US-11: Stock summary
    private int totalStockItems;
    private int lowStockItems;
    private int outOfStockItems;

    // US-12: Active reservations
    private long activeReservationsCount;
}