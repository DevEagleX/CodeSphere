package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CategoryResponse {
    private UUID id;
    private UUID workspaceId;
    private String name;
    private String slug;
    private String description;
    private LocalDateTime createdAt;
}
