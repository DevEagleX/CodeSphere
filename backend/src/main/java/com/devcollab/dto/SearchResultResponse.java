package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class SearchResultResponse {
    private String entityType; // DISCUSSION, KB_ARTICLE, DOCUMENT
    private UUID entityId;
    private String title;
    private String snippet;
    private LocalDateTime createdAt;
}
