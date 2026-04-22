package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, String> {
    Optional<Medicine> findFirstByNameIgnoreCase(String name);
}