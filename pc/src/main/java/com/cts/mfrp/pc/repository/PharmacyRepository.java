package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, String> {
}