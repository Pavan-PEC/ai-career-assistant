package com.pavan.ai_service.agent;

import com.pavan.ai_service.dto.RagSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MultiAgentRagWorkflow {

    private final ResearchAgent researchAgent;
    private final PlanningAgent planningAgent;
    private final ReviewAgent reviewAgent;
    private final ResponseAgent responseAgent;

    public MultiAgentResponse run(String question, List<RagSource> sources) {
        AgentResult research = researchAgent.run(question, sources);
        AgentResult plan = planningAgent.run(question, research);
        AgentResult review = reviewAgent.run(question, research, plan);
        AgentResult response = responseAgent.run(question, research, plan, review);
        return new MultiAgentResponse(
                response.output(),
                List.of(research, plan, review, response)
        );
    }
}
