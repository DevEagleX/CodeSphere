package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class DocumentResponse {
    private UUID id;
    private UUID workspaceId;
    private UUID parentDocumentId;
    private UUID createdById;
    private String createdByUsername;
    private UUID lastEditedById;
    private String lastEditedByUsername;
    private String title;
    private String body;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
