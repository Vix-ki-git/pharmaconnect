package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.model.PharmacyDocument;
import com.cts.mfrp.pc.service.AdminApprovalService; // 1. Import the NEW service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/auth/admin/documents")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminDocumentController {

    // 2. Change the service type to AdminApprovalService
    @Autowired
    private AdminApprovalService adminService;

    @GetMapping("/pending")
    public ResponseEntity<List<PharmacyDocument>> getPendingApplications() {
        // 3. Call the method from the new service
        List<PharmacyDocument> pendingList = adminService.getPendingDocuments();

        if (pendingList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(pendingList);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PharmacyDocument> updateStatus(
            @PathVariable String id,
            @RequestParam String status) {

        // 4. Validation remains the same
        if (!status.equalsIgnoreCase("APPROVED") && !status.equalsIgnoreCase("REJECTED")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status must be 'APPROVED' or 'REJECTED'");
        }

        // 5. Call the update method from the new adminService
        PharmacyDocument updatedDoc = adminService.updateDocumentStatus(id, status.toUpperCase());

        return ResponseEntity.ok(updatedDoc);
    }
}