package com.pavan.ai_service.agent;

import com.pavan.ai_service.service.OpenAiChatClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponseAgent {

    private final OpenAiChatClient chatClient;

    public AgentResult run(String question, AgentResult research, AgentResult plan, AgentResult review) {
        String output = chatClient.complete(
                """
                        You are a response agent for a career assistant.
                        Produce a concise, grounded final answer.
                        Use only the provided evidence and mention source filenames when useful.
                        If evidence is insufficient, say so clearly.
                        """,
                "Question:\n" + question
                        + "\n\nResearch:\n" + research.output()
                        + "\n\nPlan:\n" + plan.output()
                        + "\n\nReview:\n" + review.output()
        );
        return new AgentResult("Response Agent", output);
    }
}
