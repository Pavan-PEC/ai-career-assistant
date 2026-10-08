package com.pavan.ai_service.event;

import java.time.Instant;

public record DocumentUploadedEvent(
        Long documentId,
        Long userId,
        String originalFilename,
        String contentType,
        long size,
        String storagePath,
        Instant uploadedAt
) {
}
