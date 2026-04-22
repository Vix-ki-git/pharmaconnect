package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {
    List<Reservation> findByStatusAndCreatedAtBefore(String status, LocalDateTime time);

    List<Reservation> findByStatusAndExpiresAtBefore(String pending, LocalDateTime now);
}