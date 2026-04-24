package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.DemandAnalyticsResponseDto;
import com.cts.mfrp.pc.model.DemandAnalytics;
import com.cts.mfrp.pc.model.Medicine;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.repository.DemandAnalyticsRepository;
import com.cts.mfrp.pc.repository.MedicineRepository;
import com.cts.mfrp.pc.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandAnalyticsService {

    private final DemandAnalyticsRepository analyticsRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;

    // US-14: Called after a search result is returned — increments search count
    @Transactional
    public void recordSearch(String pharmacyId, String medicineId) {
        LocalDate today = LocalDate.now();
        DemandAnalytics record = analyticsRepository
                .findByPharmacyIdAndMedicineIdAndPeriodDate(pharmacyId, medicineId, today)
                .orElseGet(() -> createNewRecord(pharmacyId, medicineId, today));

        record.setSearchCount(record.getSearchCount() + 1);
        analyticsRepository.save(record);
    }

    // US-14: Called when a reservation is created — increments reservation count
    @Transactional
    public void recordReservation(String pharmacyId, String medicineId) {
        LocalDate today = LocalDate.now();
        DemandAnalytics record = analyticsRepository
                .findByPharmacyIdAndMedicineIdAndPeriodDate(pharmacyId, medicineId, today)
                .orElseGet(() -> createNewRecord(pharmacyId, medicineId, today));

        record.setReservationCount(record.getReservationCount() + 1);
        analyticsRepository.save(record);
    }

    // US-15: Seller views analytics for their pharmacy
    public List<DemandAnalyticsResponseDto> getPharmacyAnalytics(String pharmacyId) {
        return analyticsRepository
                .findByPharmacyIdOrderBySearchCountDescReservationCountDesc(pharmacyId)
                .stream()
                .map(a -> DemandAnalyticsResponseDto.builder()
                        .medicineId(a.getMedicine().getId())
                        .medicineName(a.getMedicine().getName())
                        .genericName(a.getMedicine().getGenericName())
                        .searchCount(a.getSearchCount())
                        .reservationCount(a.getReservationCount())
                        .periodDate(a.getPeriodDate())
                        .build())
                .collect(Collectors.toList());
    }

    private DemandAnalytics createNewRecord(String pharmacyId, String medicineId, LocalDate date) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId).orElseThrow();
        Medicine medicine = medicineRepository.findById(medicineId).orElseThrow();

        DemandAnalytics record = new DemandAnalytics();
        record.setPharmacy(pharmacy);
        record.setMedicine(medicine);
        record.setSearchCount(0);
        record.setReservationCount(0);
        record.setPeriodDate(date);
        return record;
    }
}
