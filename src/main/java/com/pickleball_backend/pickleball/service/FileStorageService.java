package com.pickleball_backend.pickleball.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final Path rootLocation = Paths.get("uploads");
    private static final long MAX_FILE_SIZE = 800 * 1024;

    public void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory: " + e.getMessage());
        }
    }

    public void delete(String filename) {
        if (filename == null || filename.isEmpty()) return;

        try {
            Path filePath = this.rootLocation.resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted file: {}", filename);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }

    // Add file validation
    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file");
            }

            // Validate file size
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("File size exceeds 800KB limit");
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Invalid file type. Only images are allowed");
            }

            // Generate unique filename
            String extension = "";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            Path destination = this.rootLocation.resolve(filename);
            Files.copy(file.getInputStream(), destination);

            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }
    public static class FileStorageException extends RuntimeException {
        public FileStorageException(String message) {
            super(message);
        }

        public FileStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class FileDeletionException extends RuntimeException {
        public FileDeletionException(String message) {
            super(message);
        }

        public FileDeletionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}