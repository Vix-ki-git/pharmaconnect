package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.PharmacyDocumentResponse;
import com.cts.mfrp.pc.service.PharmacyDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pharmacies")
public class PharmacyDocumentController {

    @Autowired
    private PharmacyDocumentService documentService;

    // Change return type to <?> to support both Success DTO and Error String
    @PostMapping("/{pharmacyId}/upload-license")
    public ResponseEntity<?> uploadLicense(
            @PathVariable String pharmacyId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("docType") String docType) {

        try {
            PharmacyDocumentResponse response = documentService.uploadLicense(pharmacyId, file, docType);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Specific check for your "Pharmacy not found" error
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}