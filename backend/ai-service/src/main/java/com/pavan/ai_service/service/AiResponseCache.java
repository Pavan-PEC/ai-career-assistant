package com.pavan.ai_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pavan.ai_service.dto.AiAnswerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiResponseCache {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rag.cache-ttl-seconds}")
    private long cacheTtlSeconds;

    public Optional<AiAnswerResponse> get(Long userId, String question) {
        String json = redisTemplate.opsForValue().get(cacheKey(userId, question));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, AiAnswerResponse.class));
        } catch (JsonProcessingException ex) {
            return Optional.empty();
        }
    }

    public void put(Long userId, String question, AiAnswerResponse response) {
        try {
            redisTemplate.opsForValue().set(
                    cacheKey(userId, question),
                    objectMapper.writeValueAsString(response),
                    Duration.ofSeconds(cacheTtlSeconds)
            );
        } catch (JsonProcessingException ignored) {
        }
    }

    private String cacheKey(Long userId, String question) {
        return "ai:rag-answer:" + userId + ":" + sha256(question == null ? "" : question);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
