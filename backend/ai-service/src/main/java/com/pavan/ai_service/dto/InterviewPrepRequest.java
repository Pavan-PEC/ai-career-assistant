package com.pavan.ai_service.dto;

public record InterviewPrepRequest(
        String targetRole,
        String experienceLevel,
        String focusArea
) {
}
