package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.DocumentSummaryDto;
import com.cts.mfrp.pc.dto.PendingSellerApplicationDto;
import com.cts.mfrp.pc.dto.SellerListItemDto;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.model.PharmacyDocument;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.repository.PharmacyDocumentRepository;
import com.cts.mfrp.pc.repository.PharmacyRepository;
import com.cts.mfrp.pc.repository.PharmacyStockRepository;
import com.cts.mfrp.pc.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminApprovalService {

    private final PharmacyDocumentRepository documentRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyStockRepository stockRepository;
    private final ReservationRepository reservationRepository;

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

    public List<PendingSellerApplicationDto> getPendingSellerApplications() {
        return pharmacyRepository.findAllByIsVerifiedFalseAndIsActiveTrue().stream()
                .map(pharmacy -> {
                    List<PharmacyDocument> docs = documentRepository.findByPharmacyId(pharmacy.getId());

                    List<DocumentSummaryDto> docDtos = docs.stream()
                            .map(doc -> DocumentSummaryDto.builder()
                                    .id(doc.getId())
                                    .documentType(doc.getDocumentType())
                                    .documentUrl(doc.getDocumentUrl())
                                    .status(doc.getStatus())
                                    .uploadedAt(doc.getUploadedAt())
                                    .build())
                            .collect(Collectors.toList());

                    String overallStatus;
                    if (docs.isEmpty()) {
                        overallStatus = "NO_DOCUMENTS";
                    } else if (docs.stream().anyMatch(d -> "PENDING".equals(d.getStatus()))) {
                        overallStatus = "PENDING";
                    } else {
                        overallStatus = "PARTIALLY_REVIEWED";
                    }

                    User owner = pharmacy.getOwner();
                    return PendingSellerApplicationDto.builder()
                            .pharmacyId(pharmacy.getId())
                            .pharmacyName(pharmacy.getName())
                            .pharmacyAddress(pharmacy.getAddress())
                            .pharmacyPhone(pharmacy.getPhone())
                            .is247(Boolean.TRUE.equals(pharmacy.getIs247()))
                            .registeredAt(pharmacy.getCreatedAt())
                            .ownerName(owner.getName())
                            .ownerEmail(owner.getEmail())
                            .ownerPhone(owner.getPhone())
                            .documents(docDtos)
                            .overallStatus(overallStatus)
                            .build();
                })
                .toList();
    }

    @Transactional
    public Map<String, String> verifyPharmacy(String pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found: " + pharmacyId));

        List<PharmacyDocument> docs = documentRepository.findByPharmacyId(pharmacyId);
        docs.forEach(doc -> doc.setStatus("APPROVED"));
        documentRepository.saveAll(docs);

        pharmacy.setIsVerified(true);
        pharmacyRepository.save(pharmacy);

        return Map.of("message", "Pharmacy verified successfully", "pharmacyId", pharmacyId);
    }

    @Transactional
    public Map<String, String> rejectPharmacy(String pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found: " + pharmacyId));

        List<PharmacyDocument> docs = documentRepository.findByPharmacyId(pharmacyId);
        docs.forEach(doc -> doc.setStatus("REJECTED"));
        documentRepository.saveAll(docs);

        pharmacy.setIsVerified(false);
        pharmacyRepository.save(pharmacy);

        return Map.of("message", "Pharmacy rejected", "pharmacyId", pharmacyId);
    }

    public List<SellerListItemDto> getAllSellers() {
        return pharmacyRepository.findAll().stream()
                .map(pharmacy -> {
                    User owner = pharmacy.getOwner();
                    int stockCount = stockRepository.findByPharmacyId(pharmacy.getId()).size();
                    int activeReservations = (int) reservationRepository.findByPharmacyId(pharmacy.getId())
                            .stream().filter(r -> "PENDING".equals(r.getStatus())).count();

                    return SellerListItemDto.builder()
                            .pharmacyId(pharmacy.getId())
                            .pharmacyName(pharmacy.getName())
                            .pharmacyAddress(pharmacy.getAddress())
                            .pharmacyPhone(pharmacy.getPhone())
                            .is247(Boolean.TRUE.equals(pharmacy.getIs247()))
                            .isVerified(pharmacy.getIsVerified())
                            .isActive(pharmacy.getIsActive())
                            .registeredAt(pharmacy.getCreatedAt())
                            .ownerName(owner != null ? owner.getName() : null)
                            .ownerEmail(owner != null ? owner.getEmail() : null)
                            .ownerPhone(owner != null ? owner.getPhone() : null)
                            .totalStockItems(stockCount)
                            .activeReservationsCount(activeReservations)
                            .build();
                })
                .toList();
    }

    @Transactional
    public Map<String, String> setPharmacyActive(String pharmacyId, boolean active) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found: " + pharmacyId));

        pharmacy.setIsActive(active);
        pharmacyRepository.save(pharmacy);

        return Map.of(
                "message", active ? "Pharmacy reactivated" : "Pharmacy deactivated",
                "pharmacyId", pharmacyId,
                "isActive", String.valueOf(active)
        );
    }
}