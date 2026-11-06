package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AnnouncementResponse {
    private UUID id;
    private UUID workspaceId;
    private String title;
    private String body;
    private boolean pinned;
    private UUID authorId;
    private String authorUsername;
    private LocalDateTime createdAt;
}
