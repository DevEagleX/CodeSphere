package com.devcollab.service;

import com.devcollab.exception.FileValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Local-disk file storage, kept behind this interface-shaped service so a future
 * S3-backed implementation can be swapped in without touching ResourceService or
 * any controller - only this class would change (Feature 12 architecture note).
 */
@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${storage.local-path}")
    private String basePath;

    public String store(MultipartFile file, String storageKey) {
        try {
            Path root = Paths.get(basePath).toAbsolutePath().normalize();
            Files.createDirectories(root);

            // storageKey is always a server-generated UUID (see ResourceService) -
            // never derived from the client-supplied filename, which prevents path
            // traversal via a crafted filename like "../../etc/passwd".
            Path target = root.resolve(storageKey).normalize();
            if (!target.startsWith(root)) {
                throw new FileValidationException("Invalid storage path");
            }

            file.transferTo(target);
            return storageKey;
        } catch (IOException e) {
            throw new FileValidationException("Failed to store file: " + e.getMessage());
        }
    }

    public Resource load(String storageKey) {
        try {
            Path root = Paths.get(basePath).toAbsolutePath().normalize();
            Path target = root.resolve(storageKey).normalize();

            if (!target.startsWith(root)) {
                throw new FileValidationException("Invalid storage path");
            }

            Resource resource = new UrlResource(target.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new FileValidationException("File not found in storage");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new FileValidationException("Failed to load file: " + e.getMessage());
        }
    }

    public static String generateStorageKey() {
        return UUID.randomUUID().toString();
    }
}
