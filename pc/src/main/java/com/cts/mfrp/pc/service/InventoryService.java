package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.InventoryResponseDTO;
import com.cts.mfrp.pc.model.Medicine;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.repository.PharmacyStockRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private PharmacyStockRepository stockRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public InventoryResponseDTO addNewInventoryItem(PharmacyStock newStock) {
        String pharmacyId = newStock.getPharmacy().getId();
        String medicineId = newStock.getMedicine().getId();

        Optional<PharmacyStock> exactMatch = stockRepository
                .findAllByPharmacyIdAndMedicineId(pharmacyId, medicineId)
                .stream()
                .filter(s -> Objects.equals(s.getManufacturingDate(), newStock.getManufacturingDate()))
                .filter(s -> Objects.equals(s.getExpiryDate(), newStock.getExpiryDate()))
                .filter(s -> Objects.equals(s.getPrice(), newStock.getPrice()))
                .findFirst();

        if (exactMatch.isPresent()) {
            PharmacyStock existing = exactMatch.get();
            existing.setQuantity(existing.getQuantity() + newStock.getQuantity());
            return mapToDTO(stockRepository.save(existing));
        }

        newStock.setPharmacy(entityManager.getReference(Pharmacy.class, pharmacyId));
        newStock.setMedicine(entityManager.getReference(Medicine.class, medicineId));
        return mapToDTO(stockRepository.save(newStock));
    }
    public List<InventoryResponseDTO> getStockByPharmacy(String pharmacyId) {
        List<PharmacyStock> stocks = stockRepository.findByPharmacyId(pharmacyId);

        // Convert the list of Entities to a list of DTOs
        return stocks.stream()
                .map(this::mapToDTO) // Uses the private helper method we wrote earlier
                .toList();
    }

    // ... (getStockByPharmacy remains the same)

    @Transactional
    public InventoryResponseDTO updateStockQuantity(String stockId, Integer newQuantity) {
        PharmacyStock stock = findStockById(stockId);
        if (newQuantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");

        stock.setQuantity(newQuantity);
        return mapToDTO(stockRepository.save(stock));
    }

    @Transactional
    public InventoryResponseDTO updateProductPrice(String stockId, Double newPrice) {
        PharmacyStock stock = findStockById(stockId);
        stock.setPrice(newPrice);
        return mapToDTO(stockRepository.save(stock));
    }

    @Transactional
    public InventoryResponseDTO updateStockDelta(String stockId, Integer delta) {
        PharmacyStock stock = findStockById(stockId);
        int updatedQuantity = stock.getQuantity() + delta;

        if (updatedQuantity < 0) {
            throw new IllegalArgumentException("Insufficient stock! Current: " + stock.getQuantity());
        }

        stock.setQuantity(updatedQuantity);
        return mapToDTO(stockRepository.save(stock));
    }

    // US-13: Delete a stock item
    @Transactional
    public void deleteStockItem(String stockId) {
        if (!stockRepository.existsById(stockId)) {
            throw new EntityNotFoundException("Stock record not found with ID: " + stockId);
        }
        stockRepository.deleteById(stockId);
    }

    // ===============================
    // PRIVATE HELPER METHODS
    // ===============================

    private PharmacyStock findStockById(String stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException("Stock record not found with ID: " + stockId));
    }

    private InventoryResponseDTO mapToDTO(PharmacyStock stock) {
        return InventoryResponseDTO.builder()
                .stockId(stock.getId())
                .medicineId(stock.getMedicine().getId())
                .medicineName(stock.getMedicine().getName())
                .genericName(stock.getMedicine().getGenericName())
                .manufacturer(stock.getMedicine().getManufacturer())
                .quantity(stock.getQuantity())
                .price(stock.getPrice())
                .lastUpdated(stock.getLastUpdated())
                .manufacturingDate(stock.getManufacturingDate())
                .expiryDate(stock.getExpiryDate())
                .build();
    }
}