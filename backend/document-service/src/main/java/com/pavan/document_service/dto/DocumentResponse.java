package com.pavan.document_service.dto;

import com.pavan.document_service.entity.Document;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        Long userId,
        String originalFilename,
        String contentType,
        long size,
        String status,
        Instant uploadedAt
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getUserId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getSize(),
                document.getStatus().name(),
                document.getUploadedAt()
        );
    }
}
