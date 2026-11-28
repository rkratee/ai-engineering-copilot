package com.kratee.copilot.document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<Document> findByIdAndOwnerId(UUID id, Long ownerId);
}