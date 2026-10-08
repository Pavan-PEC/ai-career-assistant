package com.pavan.ai_service.controller;

import com.pavan.ai_service.dto.AiAnswerResponse;
import com.pavan.ai_service.dto.AiQuestionRequest;
import com.pavan.ai_service.dto.InterviewPrepRequest;
import com.pavan.ai_service.dto.LearningPlanRequest;
import com.pavan.ai_service.dto.ResumeReviewRequest;
import com.pavan.ai_service.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final AiAssistantService aiAssistantService;

    @GetMapping("/health")
    public String health() {
        return "AI Service running";
    }

    @PostMapping("/ask")
    public AiAnswerResponse ask(@RequestBody AiQuestionRequest request) {
        return aiAssistantService.answer(request);
    }

    @PostMapping("/learning-plan")
    public Map<String, Object> learningPlan(@RequestBody LearningPlanRequest request) {
        return aiAssistantService.learningPlan(request);
    }

    @PostMapping("/resume-review")
    public Map<String, Object> resumeReview(@RequestBody ResumeReviewRequest request) {
        return aiAssistantService.resumeReview(request);
    }

    @PostMapping("/interview-prep")
    public Map<String, Object> interviewPrep(@RequestBody InterviewPrepRequest request) {
        return aiAssistantService.interviewPrep(request);
    }
}
