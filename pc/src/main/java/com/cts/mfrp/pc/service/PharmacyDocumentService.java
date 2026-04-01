package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.PharmacyDocumentResponse;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.model.PharmacyDocument;
import com.cts.mfrp.pc.repository.PharmacyDocumentRepository;
import com.cts.mfrp.pc.repository.PharmacyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class PharmacyDocumentService {

    @Autowired
    private PharmacyDocumentRepository documentRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    private final String UPLOAD_DIR = "uploads/licenses/";

    public PharmacyDocumentResponse uploadLicense(String pharmacyId, MultipartFile file, String docType) throws IOException {

        // 1. Validation: Check if file is empty
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot upload an empty file");
        }

        // 2. Validation: Check File Extension (Security Best Practice)
        String fileName = file.getOriginalFilename();
        if (fileName != null && !isAllowedExtension(fileName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG, PNG, and PDF files are allowed");
        }

        // 3. Validate if Pharmacy exists
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pharmacy not found with ID: " + pharmacyId));

        // 4. Prepare directory
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 5. Security: Generate unique filename to avoid filename collisions
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        Path filePath = uploadPath.resolve(uniqueFileName);

        // 6. Physical Save to File System
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 7. Database Save (Entity Creation)
        PharmacyDocument doc = new PharmacyDocument();
        doc.setPharmacy(pharmacy);
        doc.setDocumentType(docType);
        doc.setDocumentUrl(filePath.toString());
        doc.setStatus("PENDING"); // Initial status for Admin Approval View

        // Note: pharmacy.is_verified remains FALSE here (Requirement Gate)

        PharmacyDocument savedDoc = documentRepository.save(doc);

        // 8. Return DTO for the API Response
        return new PharmacyDocumentResponse(
                savedDoc.getId(),
                savedDoc.getPharmacy().getId(),
                savedDoc.getDocumentType(),
                savedDoc.getStatus(),
                savedDoc.getUploadedAt(),
                "License uploaded successfully and is pending verification."
        );
    }

    private boolean isAllowedExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "pdf");
        return allowedExtensions.contains(extension);
    }
}