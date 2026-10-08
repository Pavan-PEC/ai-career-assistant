package com.pavan.document_service.controller;

import com.pavan.document_service.dto.DocumentResponse;
import com.pavan.document_service.entity.Document;
import com.pavan.document_service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/health")
    public String health() {
        return "Document Service running";
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Document document = documentService.upload(file, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DocumentResponse.from(document));
    }

    @GetMapping
    public List<DocumentResponse> list(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return documentService.listForUser(userId)
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable Long id) {
        return DocumentResponse.from(documentService.get(id));
    }
}
