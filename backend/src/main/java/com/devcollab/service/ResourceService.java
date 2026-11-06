package com.devcollab.service;

import com.devcollab.dto.ResourceResponse;
import com.devcollab.entity.User;
import com.devcollab.entity.WorkspaceMember;
import com.devcollab.exception.FileValidationException;
import com.devcollab.exception.ForbiddenOperationException;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.repository.ResourceRepository;
import com.devcollab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {

    // Whitelist, not blacklist - only these content types are accepted, per Feature
    // 12's scope (PDF, ZIP, images). Anything else (e.g. executables, scripts) is
    // rejected outright, which is a meaningfully safer default than trying to
    // blocklist dangerous types.
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/zip",
            "application/x-zip-compressed",
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp"
    );

    @Value("${storage.max-file-size-bytes}")
    private long maxFileSizeBytes;

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final StorageService storageService;

    @Transactional
    public ResourceResponse upload(String username, UUID workspaceId, MultipartFile file) {
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);
        User uploader = findUser(username);

        validateFile(file);

        String storageKey = StorageService.generateStorageKey();
        storageService.store(file, storageKey);

        com.devcollab.entity.Resource resource = com.devcollab.entity.Resource.builder()
                .workspace(membership.getWorkspace())
                .uploadedBy(uploader)
                .originalFilename(sanitizeFilenameForDisplay(file.getOriginalFilename()))
                .storageKey(storageKey)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .build();
        resourceRepository.save(resource);

        return toResponse(resource);
    }

    public List<ResourceResponse> list(String username, UUID workspaceId) {
        workspaceService.requireMembership(workspaceId, username);
        return resourceRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream()
                .map(this::toResponse)
                .toList();
    }

    public com.devcollab.entity.Resource getMetadata(String username, UUID workspaceId, UUID resourceId) {
        workspaceService.requireMembership(workspaceId, username);
        return resourceRepository.findByIdAndWorkspaceId(resourceId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));
    }

    public Resource download(String username, UUID workspaceId, UUID resourceId) {
        com.devcollab.entity.Resource resource = getMetadata(username, workspaceId, resourceId);
        return storageService.load(resource.getStorageKey());
    }

    @Transactional
    public void delete(String username, UUID workspaceId, UUID resourceId) {
        com.devcollab.entity.Resource resource = getMetadata(username, workspaceId, resourceId);
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);

        boolean isUploader = resource.getUploadedBy().getUsername().equals(username);
        boolean isAdminOrOwner = membership.getRole() == WorkspaceMember.WorkspaceRole.OWNER
                || membership.getRole() == WorkspaceMember.WorkspaceRole.ADMIN;

        if (!isUploader && !isAdminOrOwner) {
            throw new ForbiddenOperationException("Only the uploader or a workspace admin/owner can delete this resource");
        }

        // Note: this deletes the DB row but not (yet) the on-disk file - a background
        // cleanup job for orphaned storage keys is a reasonable follow-up, flagged
        // rather than silently deferred.
        resourceRepository.delete(resource);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("No file provided");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new FileValidationException("File exceeds maximum allowed size of " + (maxFileSizeBytes / 1024 / 1024) + "MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileValidationException("Unsupported file type: " + contentType
                    + ". Allowed types: PDF, ZIP, PNG, JPEG, GIF, WEBP");
        }
    }

    // Client-supplied filenames are for display only - never used as a storage
    // path - but we still strip path-separator characters before storing the
    // display string, defense in depth against it being misused downstream.
    private String sanitizeFilenameForDisplay(String filename) {
        if (filename == null) return "unnamed";
        return filename.replaceAll("[/\\\\]", "_");
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private ResourceResponse toResponse(com.devcollab.entity.Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .workspaceId(resource.getWorkspace().getId())
                .originalFilename(resource.getOriginalFilename())
                .contentType(resource.getContentType())
                .sizeBytes(resource.getSizeBytes())
                .uploadedById(resource.getUploadedBy().getId())
                .uploadedByUsername(resource.getUploadedBy().getUsername())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
