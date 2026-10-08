package com.pavan.ai_service.service;

import com.pavan.ai_service.agent.MultiAgentRagWorkflow;
import com.pavan.ai_service.agent.MultiAgentResponse;
import com.pavan.ai_service.dto.AiAnswerResponse;
import com.pavan.ai_service.dto.AiQuestionRequest;
import com.pavan.ai_service.dto.InterviewPrepRequest;
import com.pavan.ai_service.dto.LearningPlanRequest;
import com.pavan.ai_service.dto.RagSource;
import com.pavan.ai_service.dto.ResumeReviewRequest;
import com.pavan.ai_service.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private final DocumentKnowledgeService documentKnowledgeService;
    private final MultiAgentRagWorkflow multiAgentRagWorkflow;
    private final AiResponseCache aiResponseCache;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.document-exchange}")
    private String exchange;

    @Value("${app.rabbitmq.notification-routing-key}")
    private String notificationRoutingKey;

    public AiAnswerResponse answer(AiQuestionRequest request) {
        return aiResponseCache.get(request.userId(), request.question())
                .orElseGet(() -> generateAnswer(request));
    }

    private AiAnswerResponse generateAnswer(AiQuestionRequest request) {
        List<RagSource> sources = documentKnowledgeService.retrieve(request.userId(), request.question());
        MultiAgentResponse multiAgentResponse = multiAgentRagWorkflow.run(request.question(), sources);

        publishNotification(request.userId(), "AI_ANSWER_READY", "Your AI answer is ready.");

        AiAnswerResponse response = new AiAnswerResponse(
                multiAgentResponse.finalAnswer(),
                sources,
                multiAgentResponse.steps().stream().map(step -> step.agentName()).toList()
        );
        aiResponseCache.put(request.userId(), request.question(), response);
        return response;
    }

    public Map<String, Object> learningPlan(LearningPlanRequest request) {
        int weeks = request.weeks() <= 0 ? 4 : request.weeks();
        List<String> plan = new ArrayList<>();
        for (int week = 1; week <= weeks; week++) {
            plan.add("Week " + week + ": study " + request.goal()
                    + ", build one practical exercise, and review progress.");
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("goal", request.goal());
        response.put("currentLevel", request.currentLevel());
        response.put("weeks", weeks);
        response.put("plan", plan);
        response.put("agents", List.of("Planner Agent", "Reviewer Agent", "Response Agent"));
        return response;
    }

    public Map<String, Object> resumeReview(ResumeReviewRequest request) {
        String resume = request.resumeText() == null ? "" : request.resumeText();
        List<String> suggestions = new ArrayList<>();
        if (!resume.toLowerCase().contains("project")) {
            suggestions.add("Add measurable project experience aligned with " + request.targetRole() + ".");
        }
        if (!resume.toLowerCase().contains("impact")) {
            suggestions.add("Add impact metrics such as latency reduced, users served, or cost saved.");
        }
        suggestions.add("Tailor skills and summary toward " + request.targetRole() + ".");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("targetRole", request.targetRole());
        response.put("score", Math.min(90, 50 + resume.length() / 40));
        response.put("suggestions", suggestions);
        response.put("agents", List.of("Reviewer Agent", "Response Agent"));
        return response;
    }

    public Map<String, Object> interviewPrep(InterviewPrepRequest request) {
        List<String> behavioral = List.of(
                "Tell me about a project where you solved a difficult technical problem.",
                "Describe a time you received critical feedback and improved.",
                "How do you prioritize when multiple deadlines conflict?"
        );
        List<String> technical = List.of(
                "Explain the architecture of a scalable " + request.targetRole() + " system.",
                "How would you debug latency in a Spring Boot microservice?",
                "How do Redis, RabbitMQ, and a database fit into a resilient backend design?"
        );
        List<String> practicePlan = List.of(
                "Day 1: revise fundamentals for " + request.focusArea() + ".",
                "Day 2: practice system design and explain tradeoffs out loud.",
                "Day 3: rehearse behavioral answers using STAR format.",
                "Day 4: do a timed mock interview and review weak areas."
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("targetRole", request.targetRole());
        response.put("experienceLevel", request.experienceLevel());
        response.put("focusArea", request.focusArea());
        response.put("behavioralQuestions", behavioral);
        response.put("technicalQuestions", technical);
        response.put("practicePlan", practicePlan);
        response.put("agents", List.of("Planning Agent", "Review Agent", "Response Agent"));
        return response;
    }

    private void publishNotification(Long userId, String type, String message) {
        rabbitTemplate.convertAndSend(
                exchange,
                notificationRoutingKey,
                new NotificationEvent(userId, type, message, Instant.now())
        );
    }
}
