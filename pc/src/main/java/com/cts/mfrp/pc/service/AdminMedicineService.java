package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.MedicineAlternativeLinkDto;
import com.cts.mfrp.pc.dto.MedicineRequestDto;
import com.cts.mfrp.pc.model.Medicine;
import com.cts.mfrp.pc.model.MedicineAlternative;
import com.cts.mfrp.pc.repository.MedicineAlternativeRepository;
import com.cts.mfrp.pc.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminMedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineAlternativeRepository medicineAlternativeRepository;

    // US-08: Add a new medicine to the master list
    public Medicine addMedicine(MedicineRequestDto dto) {
        medicineRepository.findByNameIgnoreCase(dto.getName()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Medicine already exists: " + dto.getName());
        });

        Medicine medicine = new Medicine();
        medicine.setName(dto.getName());
        medicine.setGenericName(dto.getGenericName());
        medicine.setCategory(dto.getCategory());
        medicine.setManufacturer(dto.getManufacturer());
        medicine.setDosageForm(dto.getDosageForm());
        medicine.setStrength(dto.getStrength());

        return medicineRepository.save(medicine);
    }

    // US-09: View all medicines in the master list
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    // US-10: Link two medicines as alternatives of each other (bidirectional)
    public Map<String, String> linkAlternatives(MedicineAlternativeLinkDto dto) {
        Medicine medicine = medicineRepository.findById(dto.getMedicineId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Medicine not found: " + dto.getMedicineId()));

        Medicine alternative = medicineRepository.findById(dto.getAlternativeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Alternative medicine not found: " + dto.getAlternativeId()));

        // Create both directions so alternatives search works both ways
        MedicineAlternative forward = new MedicineAlternative();
        forward.setMedicine(medicine);
        forward.setAlternative(alternative);
        forward.setEquivalenceNote(dto.getEquivalenceNote());

        MedicineAlternative reverse = new MedicineAlternative();
        reverse.setMedicine(alternative);
        reverse.setAlternative(medicine);
        reverse.setEquivalenceNote(dto.getEquivalenceNote());

        medicineAlternativeRepository.save(forward);
        medicineAlternativeRepository.save(reverse);

        return Map.of(
                "message", "Medicines linked as alternatives successfully",
                "medicine", medicine.getName(),
                "alternative", alternative.getName()
        );
    }
}
