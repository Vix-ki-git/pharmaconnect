package com.cts.mfrp.pc.dto;

import lombok.Data;

@Data
public class SellerPortalDashboardResponseDto {
    private String pharmacyId;
    private String pharmacyName;
    private Boolean isPharmacyVerified;
    private Boolean isPharmacyActive;
    private String portalAccessMessage;
}