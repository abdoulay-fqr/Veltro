package com.veltro.user.service;

import com.veltro.user.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Stores avatar images on the local filesystem under /uploads/avatars/.
 * Returns a URL path that can be served statically.
 *
 * NOTE: For production this would be replaced with S3/cloud storage.
 * For demo purposes local storage is sufficient.
 */
@Service
public class AvatarStorageService {

    private static final String UPLOAD_DIR = "uploads/avatars";
    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/webp"};

    public String store(MultipartFile file, String prefix) {
        validate(file);

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            String extension = getExtension(file.getOriginalFilename());
            String filename = prefix + "_" + UUID.randomUUID() + extension;
            Path target = uploadPath.resolve(filename);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/avatars/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store avatar file", e);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResourceNotFoundException("Avatar file is empty");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Avatar file exceeds 2 MB limit");
        }
        String contentType = file.getContentType();
        for (String allowed : ALLOWED_TYPES) {
            if (allowed.equals(contentType)) return;
        }
        throw new IllegalArgumentException("Only JPEG, PNG, and WebP images are allowed");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}