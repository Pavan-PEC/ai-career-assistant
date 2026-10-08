package com.pavan.ai_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiEmbeddingClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public OpenAiEmbeddingClient(
            RestClient restClient,
            @Value("${app.openai.api-key}") String apiKey,
            @Value("${app.openai.base-url}") String baseUrl,
            @Value("${app.openai.embedding-model}") String model) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    public List<Double> embed(String input) {
        requireApiKey();
        EmbeddingResponse response = restClient.post()
                .uri(baseUrl + "/embeddings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(Map.of("model", model, "input", input))
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new IllegalStateException("OpenAI embedding response did not contain vectors");
        }
        return response.data().getFirst().embedding();
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is required for the production RAG pipeline");
        }
    }

    public record EmbeddingResponse(List<EmbeddingData> data) {
    }

    public record EmbeddingData(List<Double> embedding) {
    }
}
