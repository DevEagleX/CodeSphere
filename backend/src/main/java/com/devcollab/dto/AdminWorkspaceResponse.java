package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AdminWorkspaceResponse {
    private UUID id;
    private String name;
    private String slug;
    private String ownerUsername;
    private long memberCount;
    private LocalDateTime createdAt;
}
