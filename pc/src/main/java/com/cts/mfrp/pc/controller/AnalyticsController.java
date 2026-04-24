package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.DemandDTO;
import com.cts.mfrp.pc.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:4200") // Matches your Angular port
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/top-demand/{pharmacyId}")
    public ResponseEntity<List<DemandDTO>> getTopDemand(@PathVariable String pharmacyId) {
        return ResponseEntity.ok(analyticsService.getTopDemandForPharmacy(pharmacyId));
    }
}