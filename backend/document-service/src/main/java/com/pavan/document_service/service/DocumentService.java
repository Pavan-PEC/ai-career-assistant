package com.pavan.document_service.service;

import com.pavan.document_service.entity.Document;
import com.pavan.document_service.entity.DocumentStatus;
import com.pavan.document_service.event.DocumentUploadedEvent;
import com.pavan.document_service.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.storage.location}")
    private String storageLocation;

    @Value("${app.rabbitmq.document-exchange}")
    private String documentExchange;

    @Value("${app.rabbitmq.document-uploaded-routing-key}")
    private String documentUploadedRoutingKey;

    public Document upload(MultipartFile file, Long userId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file must not be empty");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + "-" + originalFilename;
        Path storageDirectory = Path.of(storageLocation).toAbsolutePath().normalize();
        Path target = storageDirectory.resolve(storedFilename).normalize();

        try {
            Files.createDirectories(storageDirectory);
            file.transferTo(target);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not store uploaded file", ex);
        }

        Document document = new Document();
        document.setUserId(userId);
        document.setOriginalFilename(originalFilename);
        document.setStoredFilename(storedFilename);
        document.setContentType(file.getContentType());
        document.setSize(file.getSize());
        document.setStoragePath(target.toString());
        document.setStatus(DocumentStatus.UPLOADED);
        document.setUploadedAt(Instant.now());

        Document saved = documentRepository.save(document);
        publishUploadedEvent(saved);
        return saved;
    }

    public List<Document> listForUser(Long userId) {
        if (userId == null) {
            return documentRepository.findAll();
        }
        return documentRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }

    public Document get(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }

    private void publishUploadedEvent(Document document) {
        DocumentUploadedEvent event = new DocumentUploadedEvent(
                document.getId(),
                document.getUserId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getSize(),
                document.getStoragePath(),
                document.getUploadedAt()
        );
        rabbitTemplate.convertAndSend(documentExchange, documentUploadedRoutingKey, event);
    }
}
