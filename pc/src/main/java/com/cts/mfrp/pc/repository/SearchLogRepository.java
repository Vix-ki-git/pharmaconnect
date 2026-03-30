package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, String> {
}