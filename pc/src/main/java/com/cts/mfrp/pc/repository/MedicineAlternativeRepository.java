package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.MedicineAlternative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineAlternativeRepository extends JpaRepository<MedicineAlternative, String> {
}