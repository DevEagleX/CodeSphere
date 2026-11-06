package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class DocumentVersionResponse {
    private UUID id;
    private int versionNumber;
    private String title;
    private String body;
    private UUID editedById;
    private String editedByUsername;
    private LocalDateTime createdAt;
}
