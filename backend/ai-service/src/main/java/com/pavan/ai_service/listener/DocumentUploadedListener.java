package com.pavan.ai_service.listener;

import com.pavan.ai_service.event.DocumentUploadedEvent;
import com.pavan.ai_service.service.DocumentKnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentUploadedListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentUploadedListener.class);
    private final DocumentKnowledgeService documentKnowledgeService;

    public DocumentUploadedListener(DocumentKnowledgeService documentKnowledgeService) {
        this.documentKnowledgeService = documentKnowledgeService;
    }

    @RabbitListener(queues = "${app.rabbitmq.document-uploaded-queue}")
    public void handleDocumentUploaded(DocumentUploadedEvent event) {
        documentKnowledgeService.index(event);
        log.info(
                "Received document upload event: documentId={}, userId={}, filename={}, path={}",
                event.documentId(),
                event.userId(),
                event.originalFilename(),
                event.storagePath()
        );
    }
}
