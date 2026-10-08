package com.pavan.ai_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiChatClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public OpenAiChatClient(
            RestClient restClient,
            @Value("${app.openai.api-key}") String apiKey,
            @Value("${app.openai.base-url}") String baseUrl,
            @Value("${app.openai.chat-model}") String model) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    public String complete(String systemPrompt, String userPrompt) {
        requireApiKey();
        ChatResponse response = restClient.post()
                .uri(baseUrl + "/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(Map.of(
                        "model", model,
                        "temperature", 0.2,
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", userPrompt)
                        )
                ))
                .retrieve()
                .body(ChatResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("OpenAI chat response did not contain choices");
        }
        return response.choices().getFirst().message().content();
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is required for the production RAG pipeline");
        }
    }

    public record ChatResponse(List<Choice> choices) {
    }

    public record Choice(Message message) {
    }

    public record Message(String content) {
    }
}
