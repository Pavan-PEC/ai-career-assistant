package com.pavan.ai_service.service;

import com.pavan.ai_service.dto.RagSource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class QdrantVectorStore {

    private final RestClient restClient;
    private final String qdrantUrl;
    private final String collection;
    private final int vectorSize;

    public QdrantVectorStore(
            RestClient restClient,
            @Value("${app.qdrant.url}") String qdrantUrl,
            @Value("${app.qdrant.collection}") String collection,
            @Value("${app.qdrant.vector-size}") int vectorSize) {
        this.restClient = restClient;
        this.qdrantUrl = qdrantUrl;
        this.collection = collection;
        this.vectorSize = vectorSize;
    }

    @PostConstruct
    public void ensureCollection() {
        restClient.put()
                .uri(qdrantUrl + "/collections/" + collection)
                .body(Map.of(
                        "vectors", Map.of(
                                "size", vectorSize,
                                "distance", "Cosine"
                        )
                ))
                .retrieve()
                .toBodilessEntity();
    }

    public void upsert(Long userId, Long documentId, String filename, List<EmbeddedChunk> chunks) {
        List<Map<String, Object>> points = new ArrayList<>();
        for (EmbeddedChunk chunk : chunks) {
            points.add(Map.of(
                    "id", UUID.randomUUID().toString(),
                    "vector", chunk.embedding(),
                    "payload", Map.of(
                            "userId", userId,
                            "documentId", documentId,
                            "filename", filename,
                            "chunkIndex", chunk.chunkIndex(),
                            "text", chunk.text()
                    )
            ));
        }

        restClient.put()
                .uri(qdrantUrl + "/collections/" + collection + "/points?wait=true")
                .body(Map.of("points", points))
                .retrieve()
                .toBodilessEntity();
    }

    public List<RagSource> search(Long userId, List<Double> queryVector, int limit) {
        Map<String, Object> filter = Map.of(
                "must", List.of(Map.of(
                        "key", "userId",
                        "match", Map.of("value", userId)
                ))
        );

        QdrantSearchResponse response = restClient.post()
                .uri(qdrantUrl + "/collections/" + collection + "/points/search")
                .body(Map.of(
                        "vector", queryVector,
                        "limit", limit,
                        "with_payload", true,
                        "filter", filter
                ))
                .retrieve()
                .body(QdrantSearchResponse.class);

        if (response == null || response.result() == null) {
            return List.of();
        }

        return response.result().stream()
                .map(point -> {
                    Map<String, Object> payload = point.payload();
                    return new RagSource(
                            toLong(payload.get("documentId")),
                            String.valueOf(payload.get("filename")),
                            toInt(payload.get("chunkIndex")),
                            point.score(),
                            String.valueOf(payload.get("text"))
                    );
                })
                .toList();
    }

    private Long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(value));
    }

    private int toInt(Object value) {
        return value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value));
    }

    public record EmbeddedChunk(int chunkIndex, String text, List<Double> embedding) {
    }

    public record QdrantSearchResponse(List<QdrantPoint> result) {
    }

    public record QdrantPoint(double score, Map<String, Object> payload) {
    }
}
