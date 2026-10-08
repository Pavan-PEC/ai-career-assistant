package com.pavan.ai_service.dto;

public record AiQuestionRequest(
        Long userId,
        String question
) {
}
