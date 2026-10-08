package com.pavan.ai_service.agent;

import java.util.List;

public record MultiAgentResponse(
        String finalAnswer,
        List<AgentResult> steps
) {
}
