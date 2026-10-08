package com.pavan.ai_service.agent;

import com.pavan.ai_service.service.OpenAiChatClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewAgent {

    private final OpenAiChatClient chatClient;

    public AgentResult run(String question, AgentResult research, AgentResult plan) {
        String output = chatClient.complete(
                "You are a review agent. Check if the plan is grounded in evidence and flag missing support.",
                "Question:\n" + question
                        + "\n\nResearch:\n" + research.output()
                        + "\n\nPlan:\n" + plan.output()
        );
        return new AgentResult("Review Agent", output);
    }
}
