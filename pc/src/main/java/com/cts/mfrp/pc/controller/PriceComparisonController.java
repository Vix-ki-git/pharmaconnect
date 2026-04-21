package com.cts.mfrp.pc.controller;
import com.cts.mfrp.pc.dto.PriceComparisonDTO;
import com.cts.mfrp.pc.service.PriceComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/medicines")
public class PriceComparisonController {

    @Autowired
    private PriceComparisonService priceComparisonService;

    /**
     * Requirement: Aggregation query for price comparison
     * GET /api/auth/medicines/{medicineId}/compare
     */
    @GetMapping("/{medicineId}/compare")
    public ResponseEntity<List<PriceComparisonDTO>> getComparison(@PathVariable String medicineId) {
        List<PriceComparisonDTO> comparisonList = priceComparisonService.getPriceComparison(medicineId);

        if (comparisonList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(comparisonList);
    }
}