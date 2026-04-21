package com.cts.mfrp.pc.dto;

public interface MedicineSearchResult {
    String getStockId();
    String getPharmacyName();
    String getPharmacyAddress();
    Float getLat();
    Float getLng();
    String getMedicineName();
    String getGenericName();
    Integer getQuantity();
    Double getPrice();
    Double getDistance(); // Calculated field!
}