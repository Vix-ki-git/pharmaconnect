package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, String> {

    @Query(value = "SELECT query AS medicine_name, COUNT(*) AS demand_count " +
            "FROM search_log " +
            "WHERE searched_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "AND (6371 * acos(cos(radians(:pharmLat)) * cos(radians(user_lat)) * " +
            "cos(radians(user_lng) - radians(:pharmLng)) + " +
            "sin(radians(:pharmLat)) * sin(radians(user_lat)))) <= 10 " +
            "GROUP BY query " +
            "ORDER BY demand_count DESC " +
            "LIMIT 10", nativeQuery = true)
    List<Object[]> findTopDemandNearPharmacy(@Param("pharmLat") Float pharmLat,
                                             @Param("pharmLng") Float pharmLng);
}