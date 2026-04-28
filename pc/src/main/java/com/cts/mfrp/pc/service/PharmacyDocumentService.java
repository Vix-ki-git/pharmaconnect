package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.PharmacyDocumentResponse;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.model.PharmacyDocument;
import com.cts.mfrp.pc.repository.PharmacyDocumentRepository;
import com.cts.mfrp.pc.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor // Use this for clean injection
public class PharmacyDocumentService {

    private final PharmacyDocumentRepository documentRepository;
    private final PharmacyRepository pharmacyRepository;

    private final String UPLOAD_DIR = "uploads/licenses/";

    public PharmacyDocumentResponse uploadLicense(String pharmacyId, MultipartFile file, String docType) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot upload an empty file");
        }

        String fileName = file.getOriginalFilename();
        if (fileName != null && !isAllowedExtension(fileName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG, PNG, and PDF files are allowed");
        }

        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found"));

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        PharmacyDocument doc = new PharmacyDocument();
        doc.setPharmacy(pharmacy);
        doc.setDocumentType(docType);
        doc.setDocumentUrl(filePath.toString());
        doc.setStatus("PENDING");

        PharmacyDocument savedDoc = documentRepository.save(doc);

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
        return Arrays.asList("jpg", "jpeg", "png", "pdf").contains(extension);
    }
}