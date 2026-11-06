package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ArticleResponse {
    private UUID id;
    private UUID workspaceId;
    private UUID categoryId;
    private String categoryName;
    private UUID authorId;
    private String authorUsername;
    private String title;
    private String slug;
    private String body;
    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
