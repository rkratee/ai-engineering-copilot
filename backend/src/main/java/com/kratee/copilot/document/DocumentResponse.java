package com.kratee.copilot.document;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(UUID id, String fileName, long sizeBytes, DocumentStatus status, Instant createdAt) {}