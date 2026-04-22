package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.model.Medicine;
import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.dto  .MedicineSearchResult;
import com.cts.mfrp.pc.repository.MedicineRepository;
import com.cts.mfrp.pc.repository.PharmacyStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final PharmacyStockRepository stockRepository;
    private final MedicineRepository medicineRepository;

    // ==========================================
    // DEV 1: SEARCH EPIC METHODS
    // ==========================================

    // 1. Spatial Search API (Closest Pharmacies using GPS)
    public List<MedicineSearchResult> searchClosestMedicines(String keyword, Float lat, Float lng) {
        return stockRepository.findClosestPharmaciesWithStock(keyword, lat, lng);
    }

    // 2. Keyword Search & Price Comparison (Cheapest across all local pharmacies)
    public List<PharmacyStock> searchByKeyword(String keyword) {
        return stockRepository.findAvailableMedicineByKeyword(keyword);
    }

    // 3. Exact Medicine ID Price Comparison (From your original code)
    public List<PharmacyStock> getCheapestByMedicineId(String medicineId) {
        return stockRepository.findPharmaciesOrderByPriceAsc(medicineId);
    }

    // 4. Generic Alternative Finder
    public List<PharmacyStock> findAlternatives(String brandName) {
        // Step 1: Find the target medicine in the DB to get its generic/chemical name
        Optional<Medicine> targetMedicine = medicineRepository.findFirstByNameIgnoreCase(brandName);

        if (targetMedicine.isPresent() && targetMedicine.get().getGenericName() != null) {
            String genericName = targetMedicine.get().getGenericName();

            // Step 2: Search for all stock matching that generic name
            List<PharmacyStock> allGenericStock = stockRepository.findAvailableMedicineByKeyword(genericName);

            // Step 3: Filter out the expensive brand the user originally searched for
            return allGenericStock.stream()
                    .filter(stock -> !stock.getMedicine().getName().equalsIgnoreCase(brandName))
                    .collect(Collectors.toList());
        }

        // Return empty list if the medicine doesn't exist or has no generic equivalent
        return Collections.emptyList();
    }
}