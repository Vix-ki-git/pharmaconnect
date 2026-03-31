package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.InventoryResponseDTO;
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

    // 1. Viewing remains the same (or you can map this to a list of DTOs later)
    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<List<InventoryResponseDTO>> getPharmacyInventory(@PathVariable String pharmacyId) {
        return ResponseEntity.ok(inventoryService.getStockByPharmacy(pharmacyId));
    }

    // 2. Changed return type to InventoryResponseDTO
    @PatchMapping("/{stockId}/quantity")
    public ResponseEntity<InventoryResponseDTO> updateQuantity(
            @PathVariable String stockId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.updateStockQuantity(stockId, quantity));
    }

    // 3. Changed return type to InventoryResponseDTO
    @PatchMapping("/{stockId}/price")
    public ResponseEntity<InventoryResponseDTO> updatePrice(
            @PathVariable String stockId,
            @RequestParam Double price) {
        return ResponseEntity.ok(inventoryService.updateProductPrice(stockId, price));
    }

    // 4. Typically, adding a new item should also return a clean DTO
    @PostMapping("/add")
    public ResponseEntity<InventoryResponseDTO> addStock(@RequestBody PharmacyStock stock) {
        // Assuming you update addNewInventoryItem in Service to return DTO as well
        return ResponseEntity.status(201).body(inventoryService.addNewInventoryItem(stock));
    }

    /**
     * Requirement: Quick +/- Stock Quantity
     */
    @PatchMapping("/{stockId}/adjust-quantity")
    public ResponseEntity<InventoryResponseDTO> adjustQuantity(
            @PathVariable String stockId,
            @RequestParam Integer delta) {

        InventoryResponseDTO response = inventoryService.updateStockDelta(stockId, delta);
        return ResponseEntity.ok(response);
    }
}