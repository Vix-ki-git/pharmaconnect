package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.InventoryUploadDTO;
import com.cts.mfrp.pc.model.Medicine;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.repository.MedicineRepository;
import com.cts.mfrp.pc.repository.PharmacyStockRepository;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BulkUploadService {

    @Autowired
    private PharmacyStockRepository stockRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    public void processBulkUpload(MultipartFile file, String pharmacyId) throws Exception {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            // 1. Map CSV columns to InventoryUploadDTO list
            List<InventoryUploadDTO> dtos = new CsvToBeanBuilder<InventoryUploadDTO>(reader)
                    .withType(InventoryUploadDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            List<PharmacyStock> stocksToSave = new ArrayList<>();

            // 2. Loop through each DTO row and transform to Entity
            for (InventoryUploadDTO dto : dtos) {
                // Find medicine by Name. If duplicates exist in the catalog, take the first match;
                // sellers identify medicines by display name in CSVs, not by UUID.
                Medicine medicine = medicineRepository.findFirstByNameIgnoreCase(dto.getMedicineName())
                        .orElseThrow(() -> new RuntimeException("Medicine not found in system: " + dto.getMedicineName()));

                PharmacyStock stock = new PharmacyStock();

                // Link the existing Pharmacy ID
                Pharmacy pharmacy = new Pharmacy();
                pharmacy.setId(pharmacyId);
                stock.setPharmacy(pharmacy);

                // Set data from the CSV/DTO
                stock.setMedicine(medicine);
                stock.setQuantity(dto.getQuantity());
                stock.setPrice(dto.getPrice());

                // Parse Strings to LocalDate (format must be YYYY-MM-DD in CSV)
                stock.setManufacturingDate(LocalDate.parse(dto.getManufacturingDate()));
                stock.setExpiryDate(LocalDate.parse(dto.getExpiryDate()));

                // stock.setId() is NOT called, so the DB creates a new ID for every row
                stocksToSave.add(stock);
            }

            // 3. Batch save for efficiency
            stockRepository.saveAll(stocksToSave);
        }
    }
}
