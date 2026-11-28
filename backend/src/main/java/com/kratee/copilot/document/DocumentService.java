package com.kratee.copilot.document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {
    private static final Set<String> ALLOWED = Set.of("pdf", "docx");

    private final DocumentRepository repository;
    private final FileStorageService storage;

    public DocumentService(DocumentRepository repository, FileStorageService storage) {
        this.repository = repository;
        this.storage = storage;
    }

    public DocumentResponse upload(Long ownerId, MultipartFile file) {
        String original = file.getOriginalFilename();
        if (file.isEmpty() || original == null) {
            throw new InvalidFileException("File is empty");
        }
        String name = Paths.get(original).getFileName().toString();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED.contains(ext)) {
            throw new InvalidFileException("Only PDF and DOCX files are allowed");
        }
        checkSignature(file, ext);

        Document doc = new Document();
        doc.setOwnerId(ownerId);
        doc.setFileName(name);
        doc.setContentType(file.getContentType());
        doc.setSizeBytes(file.getSize());
        Document saved = repository.save(doc);

        try {
            storage.store(saved.getId(), file);
        } catch (IOException e) {
            repository.delete(saved);
            throw new IllegalStateException("Could not store file", e);
        }
        return toResponse(saved);
    }

    public List<DocumentResponse> list(Long ownerId) {
        return repository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(this::toResponse).toList();
    }

    public void delete(Long ownerId, UUID id) {
        Document doc = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(DocumentNotFoundException::new);
        try {
            storage.delete(id);
        } catch (IOException e) {
            throw new IllegalStateException("Could not delete file", e);
        }
        repository.delete(doc);
    }

    private void checkSignature(MultipartFile file, String ext) {
        try {
            byte[] head = file.getInputStream().readNBytes(5);
            String text = new String(head, StandardCharsets.ISO_8859_1);
            boolean ok = ext.equals("pdf") ? text.startsWith("%PDF-") : text.startsWith("PK");
            if (!ok) throw new InvalidFileException("File content does not match its type");
        } catch (IOException e) {
            throw new InvalidFileException("File could not be read");
        }
    }

    private DocumentResponse toResponse(Document d) {
        return new DocumentResponse(d.getId(), d.getFileName(), d.getSizeBytes(), d.getStatus(), d.getCreatedAt());
    }
}