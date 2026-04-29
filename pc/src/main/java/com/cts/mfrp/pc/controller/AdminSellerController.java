package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.PendingSellerApplicationDto;
import com.cts.mfrp.pc.dto.SellerListItemDto;
import com.cts.mfrp.pc.service.AdminApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/admin/sellers")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AdminSellerController {

    private final AdminApprovalService adminApprovalService;

    @GetMapping("/pending")
    public ResponseEntity<List<PendingSellerApplicationDto>> getPendingSellerApplications() {
        List<PendingSellerApplicationDto> pending = adminApprovalService.getPendingSellerApplications();
        if (pending.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SellerListItemDto>> getAllSellers() {
        List<SellerListItemDto> all = adminApprovalService.getAllSellers();
        if (all.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(all);
    }

    @PatchMapping("/{pharmacyId}/verify")
    public ResponseEntity<Map<String, String>> verifyPharmacy(@PathVariable String pharmacyId) {
        return ResponseEntity.ok(adminApprovalService.verifyPharmacy(pharmacyId));
    }

    @PatchMapping("/{pharmacyId}/reject")
    public ResponseEntity<Map<String, String>> rejectPharmacy(@PathVariable String pharmacyId) {
        return ResponseEntity.ok(adminApprovalService.rejectPharmacy(pharmacyId));
    }

    @PatchMapping("/{pharmacyId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivatePharmacy(@PathVariable String pharmacyId) {
        return ResponseEntity.ok(adminApprovalService.setPharmacyActive(pharmacyId, false));
    }

    @PatchMapping("/{pharmacyId}/activate")
    public ResponseEntity<Map<String, String>> activatePharmacy(@PathVariable String pharmacyId) {
        return ResponseEntity.ok(adminApprovalService.setPharmacyActive(pharmacyId, true));
    }
}
