package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // Endpoint for pharmacy owners to see their dashboard
    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<List<PharmacyStock>> getPharmacyInventory(@PathVariable String pharmacyId) {
        return ResponseEntity.ok(inventoryService.getStockByPharmacy(pharmacyId));
    }

    // Endpoint to update stock levels
    @PatchMapping("/{stockId}/quantity")
    public ResponseEntity<PharmacyStock> updateQuantity(
            @PathVariable String stockId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.updateStockQuantity(stockId, quantity));
    }

    @PatchMapping("/{stockId}/price")
    public ResponseEntity<PharmacyStock> updatePrice(
            @PathVariable String stockId,
            @RequestParam Double price) {
        return ResponseEntity.ok(inventoryService.updateProductPrice(stockId, price));
    }

    @PostMapping("/add")
    public ResponseEntity<PharmacyStock> addStock(@RequestBody PharmacyStock stock) {
        return ResponseEntity.status(201).body(inventoryService.addNewInventoryItem(stock));
    }
}