package com.pavan.ai_service.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

@Service
public class DocumentTextExtractor {

    private final Tika tika = new Tika();

    public String extract(Path path) {
        try {
            return tika.parseToString(path);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to extract text from document: " + path, ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse document: " + path, ex);
        }
    }
}
