package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.MedicineAlternativeLinkDto;
import com.cts.mfrp.pc.dto.MedicineRequestDto;
import com.cts.mfrp.pc.model.Medicine;
import com.cts.mfrp.pc.service.AdminMedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/medicines")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AdminMedicineController {

    private final AdminMedicineService adminMedicineService;

    // US-08
    @PostMapping
    public ResponseEntity<Medicine> addMedicine(@RequestBody MedicineRequestDto dto) {
        return ResponseEntity.ok(adminMedicineService.addMedicine(dto));
    }

    // US-09
    @GetMapping
    public ResponseEntity<List<Medicine>> getAllMedicines() {
        return ResponseEntity.ok(adminMedicineService.getAllMedicines());
    }

    // US-10
    @PostMapping("/link-alternative")
    public ResponseEntity<Map<String, String>> linkAlternatives(@RequestBody MedicineAlternativeLinkDto dto) {
        return ResponseEntity.ok(adminMedicineService.linkAlternatives(dto));
    }
}
