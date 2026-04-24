package com.cts.mfrp.pc.service;
import com.cts.mfrp.pc.dto.DemandDTO;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.repository.PharmacyRepository;
import com.cts.mfrp.pc.repository.SearchLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    @Autowired
    private SearchLogRepository searchLogRepository;
    @Autowired
    private PharmacyRepository pharmacyRepository;
    public List<DemandDTO> getTopDemandForPharmacy(String pharmacyId) {

// 1. Get Pharmacy location to define the center of the demand zone
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy not found"));
        List<Object[]> results = searchLogRepository.findTopDemandNearPharmacy(
                pharmacy.getLat(),

                pharmacy.getLng()
        );
// 3. Map to DTO
        return results.stream()
                .map(result -> new DemandDTO(
                        (String) result[0],
                        ((Number) result[1]).longValue()
                ))
                .collect(Collectors.toList());

    }

}