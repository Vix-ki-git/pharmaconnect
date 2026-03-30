package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.SellerPortalDashboardResponseDto;
import com.cts.mfrp.pc.service.SellerPortalAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller-portal")
@CrossOrigin(origins = "*")
public class SellerPortalController {

    private final SellerPortalAccessService sellerPortalAccessService;

    public SellerPortalController(SellerPortalAccessService sellerPortalAccessService) {
        this.sellerPortalAccessService = sellerPortalAccessService;
    }

    // Notice we use @GetMapping and pass the email in the URL
    @GetMapping("/my-dashboard/{sellerEmailAddress}")
    public ResponseEntity<?> accessSellerDashboard(@PathVariable String sellerEmailAddress) {
        try {
            SellerPortalDashboardResponseDto dashboardData = sellerPortalAccessService.fetchDashboardDataForSeller(sellerEmailAddress);
            return ResponseEntity.ok(dashboardData);
        } catch (Exception exception) {
            return ResponseEntity.status(403).body("Access Denied: " + exception.getMessage());
        }
    }
}