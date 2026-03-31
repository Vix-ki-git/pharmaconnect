package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.PharmacyStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyStockRepository extends JpaRepository<PharmacyStock, String> {

    List<PharmacyStock> findByPharmacyId(String pharmacyId);

    Optional<PharmacyStock> findByPharmacyIdAndMedicineId(String pharmacyId, String medicineId);

    @Query("SELECT ps FROM PharmacyStock ps WHERE ps.medicine.id = :medicineId AND ps.quantity > 0")
    List<PharmacyStock> findAvailableStockByMedicine(@Param("medicineId") String medicineId);
}