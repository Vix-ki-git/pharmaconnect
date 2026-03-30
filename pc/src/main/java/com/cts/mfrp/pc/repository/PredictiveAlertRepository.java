package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.PredictiveAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PredictiveAlertRepository extends JpaRepository<PredictiveAlert, String> {
}