package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.DemandAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DemandAnalyticsRepository extends JpaRepository<DemandAnalytics, String> {
    Optional<DemandAnalytics> findByPharmacyIdAndMedicineIdAndPeriodDate(String pharmacyId, String medicineId, LocalDate periodDate);
    List<DemandAnalytics> findByPharmacyIdOrderBySearchCountDescReservationCountDesc(String pharmacyId);
}