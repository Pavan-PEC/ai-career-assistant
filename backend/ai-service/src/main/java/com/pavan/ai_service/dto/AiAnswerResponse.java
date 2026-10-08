package com.pavan.ai_service.dto;

import java.util.List;

public record AiAnswerResponse(
        String answer,
        List<RagSource> sources,
        List<String> agents
) {
}
