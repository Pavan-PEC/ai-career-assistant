package com.pavan.ai_service.dto;

public record LearningPlanRequest(
        String goal,
        String currentLevel,
        int weeks
) {
}
