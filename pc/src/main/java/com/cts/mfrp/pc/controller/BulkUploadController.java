package com.cts.mfrp.pc.controller;


import com.cts.mfrp.pc.service.BulkUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller to handle Bulk Inventory operations for Sellers.
 */
@RestController
@RequestMapping("/api/auth/seller")
@CrossOrigin(origins = "*") // Adjust as per your security requirements
public class BulkUploadController {

    @Autowired
    private BulkUploadService bulkUploadService;

    /**
     * Endpoint to upload a CSV file and add 100+ items to a pharmacy's stock.
     * * @param file       The CSV file containing medicineName, quantity, price, etc.
     * @param pharmacyId The unique ID of the pharmacy uploading the stock.
     * @return ResponseEntity with success or error message.
     */
    @PostMapping(value = "/inventory/bulk-upload-csv/{pharmacyId}", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadInventoryCsv(
            @RequestParam("file") MultipartFile file,
            @PathVariable String pharmacyId) {

        // 1. Check if a file was actually provided
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Please select a CSV file to upload.");
        }

        // 2. Validate file extension (basic security/integrity check)
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Error: Invalid file format. Only .csv files are supported.");
        }

        try {
            // 3. Delegate processing to the service layer
            bulkUploadService.processBulkUpload(file, pharmacyId);

            return ResponseEntity.ok("Successfully processed the inventory file for Pharmacy: " + pharmacyId);

        } catch (RuntimeException e) {
            // Catches specific errors like "Medicine not found" or "Parsing errors"
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Data Error: " + e.getMessage());
        } catch (Exception e) {
            // Catches generic system errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("System Error during bulk upload: " + e.getMessage());
        }
    }
}
