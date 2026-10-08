package com.pavan.ai_service.agent;

import com.pavan.ai_service.service.OpenAiChatClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanningAgent {

    private final OpenAiChatClient chatClient;

    public AgentResult run(String question, AgentResult research) {
        String output = chatClient.complete(
                "You are a planning agent. Create a concise answer plan from retrieved evidence.",
                "Question:\n" + question + "\n\nResearch evidence:\n" + research.output()
        );
        return new AgentResult("Planning Agent", output);
    }
}
