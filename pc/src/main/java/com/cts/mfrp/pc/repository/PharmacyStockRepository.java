package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.PharmacyStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PharmacyStockRepository extends JpaRepository<PharmacyStock, String> {
}