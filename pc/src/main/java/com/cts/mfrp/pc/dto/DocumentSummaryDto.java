package com.cts.mfrp.pc.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentSummaryDto {
    private String id;
    private String documentType;
    private String documentUrl;
    private String status;
    private LocalDateTime uploadedAt;
}
