package com.pavan.ai_service.agent;

import com.pavan.ai_service.dto.RagSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResearchAgent {

    public AgentResult run(String question, List<RagSource> sources) {
        StringBuilder output = new StringBuilder();
        output.append("Question: ").append(question).append("\n");
        output.append("Relevant evidence:\n");
        for (int index = 0; index < sources.size(); index++) {
            RagSource source = sources.get(index);
            output.append(index + 1)
                    .append(". ")
                    .append(source.filename())
                    .append(" chunk ")
                    .append(source.chunkIndex())
                    .append(" score ")
                    .append(source.score())
                    .append(": ")
                    .append(source.text())
                    .append("\n");
        }
        return new AgentResult("Research Agent", output.toString());
    }
}
