package com.kratee.copilot.document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private final Path root;

    public FileStorageService(@Value("${app.storage.dir}") String dir) throws IOException {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public void store(UUID id, MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(id.toString()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void delete(UUID id) throws IOException {
        Files.deleteIfExists(root.resolve(id.toString()));
    }
}