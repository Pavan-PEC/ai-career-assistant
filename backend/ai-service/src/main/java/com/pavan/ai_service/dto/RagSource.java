package com.pavan.ai_service.dto;

public record RagSource(
        Long documentId,
        String filename,
        int chunkIndex,
        double score,
        String text
) {
}
