package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.model.Medicine;
import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.dto.MedicineSearchResult;
import com.cts.mfrp.pc.repository.MedicineRepository;
import com.cts.mfrp.pc.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final MedicineRepository medicineRepository;

    /**
     * Finds closest pharmacies for a medicine based on user's GPS
     * GET /api/search/closest?keyword=paracetamol&lat=12.9716&lng=77.5946
     */
    @GetMapping("/closest")
    public ResponseEntity<List<MedicineSearchResult>> getClosestPharmacies(
            @RequestParam String keyword,
            @RequestParam Float lat,
            @RequestParam Float lng,
            @RequestParam(required = false) String userId) {

        return ResponseEntity.ok(searchService.searchClosestMedicines(keyword, lat, lng, userId));
    }

    @GetMapping("/emergency")
    public ResponseEntity<List<MedicineSearchResult>> emergencySearch(
            @RequestParam String keyword,
            @RequestParam Float lat,
            @RequestParam Float lng,
            @RequestParam(required = false) String userId) {

        return ResponseEntity.ok(searchService.emergencySearch(keyword, lat, lng, userId));
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
     * Exact search by Medicine ID sorted by lowest price
     * GET /api/search/medicine/12345-abcde/prices
     */
    @GetMapping("/medicine/{medicineId}/prices")
    public ResponseEntity<List<PharmacyStock>> getPricesByMedicineId(
            @PathVariable String medicineId) {

        return ResponseEntity.ok(searchService.getCheapestByMedicineId(medicineId));
    }

    /**
     * Finds cheaper generic equivalents for a specific brand name
     * GET /api/search/alternatives?brandName=Crocin
     */
    @GetMapping("/alternatives")
    public ResponseEntity<List<PharmacyStock>> getAlternatives(
            @RequestParam String brandName) {

        return ResponseEntity.ok(searchService.findAlternatives(brandName));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<MedicineSearchResult>> filterNearbyMedicines(
            @RequestParam String keyword,
            @RequestParam Float lat,
            @RequestParam Float lng,
            @RequestParam(required = false) Double radius) {

        return ResponseEntity.ok(searchService.filterMedicines(keyword, lat, lng, radius));
    }

    /**
     * Autocomplete suggestions — returns up to 8 medicine names matching the keyword
     * GET /api/search/suggest?keyword=para
     */
    @GetMapping("/suggest")
    public ResponseEntity<List<String>> suggest(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }
        List<Medicine> matches = medicineRepository
                .findByNameContainingIgnoreCaseOrGenericNameContainingIgnoreCase(
                        keyword.trim(), keyword.trim());
        List<String> names = matches.stream()
                .map(Medicine::getName)
                .distinct()
                .limit(8)
                .collect(Collectors.toList());
        return ResponseEntity.ok(names);
    }
}