package com.kratee.copilot.document;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService service;

    public DocumentController(DocumentService service) { this.service = service; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestParam("file") MultipartFile file) {
        Long ownerId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.upload(ownerId, file));
    }

    @GetMapping
    public List<DocumentResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.list(Long.valueOf(jwt.getSubject()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.delete(Long.valueOf(jwt.getSubject()), id);
        return ResponseEntity.noContent().build();
    }
}