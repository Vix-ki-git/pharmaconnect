package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.PharmacyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PharmacyDocumentRepository extends JpaRepository<PharmacyDocument, String> {
    // This allows the Admin Approval View to find pending docs later
}