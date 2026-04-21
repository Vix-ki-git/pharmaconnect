package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.PriceComparisonDTO;
import com.cts.mfrp.pc.model.PharmacyStock;
import com.cts.mfrp.pc.repository.PharmacyStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PriceComparisonService {

    @Autowired
    private PharmacyStockRepository pharmacyStockRepository;

    public List<PriceComparisonDTO> getPriceComparison(String medicineId) {
        List<PharmacyStock> stocks = pharmacyStockRepository.findPharmaciesOrderByPriceAsc(medicineId);

        return stocks.stream().map(stock -> new PriceComparisonDTO(
                stock.getPharmacy().getName(),
                stock.getPharmacy().getAddress(),
                stock.getPrice(),
                stock.getQuantity(),
                stock.getPharmacy().getIs247()
        )).collect(Collectors.toList());
    }
}