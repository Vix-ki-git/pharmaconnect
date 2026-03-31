package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.repository.PharmacyStockRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private PharmacyStockRepository stockRepository;

    public List<PharmacyStock> getStockByPharmacy(String pharmacyId) {
        return stockRepository.findByPharmacyId(pharmacyId);
    }

    @Transactional
    public PharmacyStock updateStockQuantity(String stockId, Integer newQuantity) {
        PharmacyStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException("Stock record not found"));

        if (newQuantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");

        stock.setQuantity(newQuantity);
        return stockRepository.save(stock);
    }

    @Transactional
    public PharmacyStock updateProductPrice(String stockId, Double newPrice) {
        PharmacyStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException("Stock record not found"));

        stock.setPrice(newPrice);
        return stockRepository.save(stock);
    }

    @Transactional
    public PharmacyStock addNewInventoryItem(PharmacyStock newStock) {
        // Check if this medicine already exists in this pharmacy to avoid duplicates
        return stockRepository.save(newStock);
    }
}

