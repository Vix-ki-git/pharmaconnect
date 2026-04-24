package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.DemandAnalyticsResponseDto;
import com.cts.mfrp.pc.dto.SellerPortalDashboardResponseDto;
import com.cts.mfrp.pc.service.DemandAnalyticsService;
import com.cts.mfrp.pc.service.SellerPortalAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller-portal")
@RequiredArgsConstructor
public class SellerPortalController {

    private final SellerPortalAccessService sellerPortalAccessService;
    private final DemandAnalyticsService demandAnalyticsService;

    @GetMapping("/my-dashboard/{sellerEmailAddress}")
    public ResponseEntity<?> accessSellerDashboard(@PathVariable String sellerEmailAddress) {
        try {
            SellerPortalDashboardResponseDto dashboardData = sellerPortalAccessService.fetchDashboardDataForSeller(sellerEmailAddress);
            return ResponseEntity.ok(dashboardData);
        } catch (Exception exception) {
            return ResponseEntity.status(403).body("Access Denied: " + exception.getMessage());
        }
    }

    // US-15
    @GetMapping("/{pharmacyId}/analytics")
    public ResponseEntity<List<DemandAnalyticsResponseDto>> getAnalytics(@PathVariable String pharmacyId) {
        return ResponseEntity.ok(demandAnalyticsService.getPharmacyAnalytics(pharmacyId));
    }
}