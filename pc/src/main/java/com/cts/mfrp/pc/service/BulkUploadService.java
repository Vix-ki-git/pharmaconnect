package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.InventoryUploadDTO;
import com.cts.mfrp.pc.model.Medicine;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.repository.MedicineRepository;
import com.cts.mfrp.pc.repository.PharmacyStockRepository;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class BulkUploadService {

    @Autowired
    private PharmacyStockRepository stockRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public String processBulkUpload(MultipartFile file, String pharmacyId) throws Exception {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            List<InventoryUploadDTO> dtos = new CsvToBeanBuilder<InventoryUploadDTO>(reader)
                    .withType(InventoryUploadDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            // Preload existing stocks for this pharmacy so we can match without extra round-trips
            List<PharmacyStock> existingStocks = stockRepository.findByPharmacyId(pharmacyId);

            LocalDate today = LocalDate.now();
            int rowNum = 1;
            int added = 0;
            int merged = 0;
            List<String> skipped = new ArrayList<>();

            for (InventoryUploadDTO dto : dtos) {
                rowNum++;

                String name = dto.getMedicineName() == null ? "" : dto.getMedicineName().trim();

                if (name.isBlank()) {
                    skipped.add("Row " + rowNum + ": missing medicine name.");
                    continue;
                }
                if (dto.getQuantity() == null || dto.getQuantity() < 1) {
                    skipped.add("Row " + rowNum + ": For '" + name + "', quantity must be at least 1.");
                    continue;
                }
                if (dto.getPrice() == null || dto.getPrice() <= 0) {
                    skipped.add("Row " + rowNum + ": For '" + name + "', price must be greater than 0.");
                    continue;
                }

                LocalDate mfgDate;
                LocalDate expDate;
                try {
                    mfgDate = (dto.getManufacturingDate() == null || dto.getManufacturingDate().isBlank())
                            ? null : LocalDate.parse(dto.getManufacturingDate());
                    expDate = (dto.getExpiryDate() == null || dto.getExpiryDate().isBlank())
                            ? null : LocalDate.parse(dto.getExpiryDate());
                } catch (DateTimeParseException e) {
                    skipped.add("Row " + rowNum + ": For '" + name + "', invalid date format (expected YYYY-MM-DD).");
                    continue;
                }

                if (mfgDate != null && mfgDate.isAfter(today)) {
                    skipped.add("Row " + rowNum + ": For '" + name + "', the manufacturing date should not be a future date.");
                    continue;
                }
                if (expDate != null && expDate.isBefore(today)) {
                    skipped.add("Row " + rowNum + ": For '" + name + "', the expiry date should not be a past date.");
                    continue;
                }
                if (mfgDate != null && expDate != null && expDate.isBefore(mfgDate)) {
                    skipped.add("Row " + rowNum + ": For '" + name + "', the expiry date must be on or after the manufacturing date.");
                    continue;
                }

                Medicine medicine = medicineRepository.findFirstByNameIgnoreCase(name).orElse(null);
                if (medicine == null) {
                    skipped.add("Row " + rowNum + ": Medicine '" + name + "' was not found in the catalog.");
                    continue;
                }

                final String medicineId = medicine.getId();
                final LocalDate mfgFinal = mfgDate;
                final LocalDate expFinal = expDate;
                final Double price = dto.getPrice();

                PharmacyStock match = existingStocks.stream()
                        .filter(s -> medicineId.equals(s.getMedicine().getId()))
                        .filter(s -> Objects.equals(s.getManufacturingDate(), mfgFinal))
                        .filter(s -> Objects.equals(s.getExpiryDate(), expFinal))
                        .filter(s -> Objects.equals(s.getPrice(), price))
                        .findFirst()
                        .orElse(null);

                if (match != null) {
                    match.setQuantity(match.getQuantity() + dto.getQuantity());
                    stockRepository.save(match);
                    merged++;
                } else {
                    PharmacyStock stock = new PharmacyStock();
                    stock.setPharmacy(entityManager.getReference(Pharmacy.class, pharmacyId));
                    stock.setMedicine(medicine);
                    stock.setQuantity(dto.getQuantity());
                    stock.setPrice(price);
                    stock.setManufacturingDate(mfgFinal);
                    stock.setExpiryDate(expFinal);
                    PharmacyStock saved = stockRepository.save(stock);
                    // include the newly-saved row in the in-memory list so subsequent duplicate
                    // rows in the same CSV merge into it instead of creating yet another row
                    existingStocks.add(saved);
                    added++;
                }
            }

            StringBuilder summary = new StringBuilder();
            summary.append("Bulk upload complete: ").append(added).append(" added, ")
                    .append(merged).append(" merged into existing");
            if (!skipped.isEmpty()) {
                summary.append(", ").append(skipped.size()).append(" skipped.\nSkipped rows:");
                for (String s : skipped) {
                    summary.append("\n  - ").append(s);
                }
            } else {
                summary.append(".");
            }
            return summary.toString();
        }
    }
}
