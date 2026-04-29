package com.cts.mfrp.pc.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SellerListItemDto {
    private String pharmacyId;
    private String pharmacyName;
    private String pharmacyAddress;
    private String pharmacyPhone;
    private boolean is247;
    private Boolean isVerified;
    private Boolean isActive;
    private LocalDateTime registeredAt;

    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;

    private int totalStockItems;
    private int activeReservationsCount;
}
