package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.model.PharmacyDocument;
import com.cts.mfrp.pc.repository.PharmacyDocumentRepository;
import com.cts.mfrp.pc.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminApprovalService {

    private final PharmacyDocumentRepository documentRepository;
    private final PharmacyRepository pharmacyRepository;

    /**
     * Fetches all documents that are currently in PENDING status for Admin review.
     */
    public List<PharmacyDocument> getPendingDocuments() {
        return documentRepository.findByStatus("PENDING");
    }

    /**
     * Updates the status of a specific document and evaluates
     * if the parent Pharmacy should be verified.
     */
    @Transactional
    public PharmacyDocument updateDocumentStatus(String id, String status) {
        String upperStatus = status.toUpperCase();

        return documentRepository.findById(id)
                .map(doc -> {
                    // 1. Update the status of the current document
                    doc.setStatus(upperStatus);
                    PharmacyDocument savedDoc = documentRepository.save(doc);

                    Pharmacy pharmacy = doc.getPharmacy();
                    if (pharmacy == null) return savedDoc;

                    // 2. Logic: Handle "All or Nothing" Verification
                    if ("APPROVED".equals(upperStatus)) {
                        // Check if ANY other documents for this pharmacy are NOT approved
                        List<PharmacyDocument> allPharmacyDocs = documentRepository.findByPharmacyId(pharmacy.getId());

                        boolean hasIncompleteDocs = allPharmacyDocs.stream()
                                .anyMatch(d -> !"APPROVED".equals(d.getStatus()));

                        // Only verify if EVERY document is now APPROVED
                        if (!hasIncompleteDocs) {
                            pharmacy.setIsVerified(true);
                            pharmacyRepository.save(pharmacy);
                        }
                    }
                    // 3. Logic: If REJECTED, immediately ensure isVerified is false
                    else if ("REJECTED".equals(upperStatus)) {
                        pharmacy.setIsVerified(false);
                        pharmacyRepository.save(pharmacy);
                    }

                    return savedDoc;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document ID not found: " + id));
    }
}