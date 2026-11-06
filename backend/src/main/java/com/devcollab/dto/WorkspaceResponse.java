package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class WorkspaceResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private UUID ownerId;
    private String ownerUsername;
    private long memberCount;
    private String currentUserRole;
    private LocalDateTime createdAt;
}
