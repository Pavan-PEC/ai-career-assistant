package com.pavan.ai_service.dto;

public record ResumeReviewRequest(
        String resumeText,
        String targetRole
) {
}
