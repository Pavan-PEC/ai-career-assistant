package com.pavan.ai_service.service;

import com.pavan.ai_service.dto.RagSource;
import com.pavan.ai_service.event.DocumentUploadedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentKnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(DocumentKnowledgeService.class);

    private final DocumentTextExtractor documentTextExtractor;
    private final TextChunker textChunker;
    private final OpenAiEmbeddingClient embeddingClient;
    private final QdrantVectorStore vectorStore;

    @Value("${app.rag.top-k}")
    private int topK;

    public void index(DocumentUploadedEvent event) {
        String text = documentTextExtractor.extract(Path.of(event.storagePath()));
        List<String> chunks = textChunker.chunk(text);
        List<QdrantVectorStore.EmbeddedChunk> embeddedChunks = new ArrayList<>();

        for (int index = 0; index < chunks.size(); index++) {
            String chunk = chunks.get(index);
            embeddedChunks.add(new QdrantVectorStore.EmbeddedChunk(
                    index,
                    chunk,
                    embeddingClient.embed(chunk)
            ));
        }

        vectorStore.upsert(event.userId(), event.documentId(), event.originalFilename(), embeddedChunks);
        log.info("Indexed document {} with {} chunks", event.documentId(), embeddedChunks.size());
    }

    public List<RagSource> retrieve(Long userId, String question) {
        List<Double> queryVector = embeddingClient.embed(question);
        return vectorStore.search(userId, queryVector, topK);
    }
}
