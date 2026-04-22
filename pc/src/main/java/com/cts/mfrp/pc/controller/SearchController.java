package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.dto.MedicineSearchResult;
import com.cts.mfrp.pc.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Finds closest pharmacies for a medicine based on user's GPS
     * GET /api/search/closest?keyword=paracetamol&lat=12.9716&lng=77.5946
     */
    @GetMapping("/closest")
    public ResponseEntity<List<MedicineSearchResult>> getClosestPharmacies(
            @RequestParam String keyword,
            @RequestParam Float lat,
            @RequestParam Float lng) {

        return ResponseEntity.ok(searchService.searchClosestMedicines(keyword, lat, lng));
    }

    /**
     * Broad search by keyword/generic name sorted by lowest price
     * GET /api/search/keyword?keyword=paracetamol
     */
    @GetMapping("/keyword")
    public ResponseEntity<List<PharmacyStock>> searchByKeyword(
            @RequestParam String keyword) {

        return ResponseEntity.ok(searchService.searchByKeyword(keyword));
    }

    /**
     * Exact search by Medicine ID sorted by lowest price (Your original logic)
     * GET /api/search/medicine/12345-abcde/prices
     */
    @GetMapping("/medicine/{medicineId}/prices")
    public ResponseEntity<List<PharmacyStock>> getPricesByMedicineId(
            @PathVariable String medicineId) {

        return ResponseEntity.ok(searchService.getCheapestByMedicineId(medicineId));
    }

    /**
     * Finds cheaper generic equivalents for a specific brand name
     * GET /api/search/alternatives?brandName=Tylenol
     */
    @GetMapping("/alternatives")
    public ResponseEntity<List<PharmacyStock>> getAlternatives(
            @RequestParam String brandName) {

        return ResponseEntity.ok(searchService.findAlternatives(brandName));
    }
}