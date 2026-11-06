package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ResourceResponse {
    private UUID id;
    private UUID workspaceId;
    private String originalFilename;
    private String contentType;
    private long sizeBytes;
    private UUID uploadedById;
    private String uploadedByUsername;
    private LocalDateTime createdAt;
}
