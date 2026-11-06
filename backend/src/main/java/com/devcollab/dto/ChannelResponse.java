package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ChannelResponse {
    private UUID id;
    private UUID workspaceId;
    private String name;
    private String description;
    private String visibility;
    private UUID createdBy;
    private String createdByUsername;
    private long memberCount;
    private LocalDateTime createdAt;
}
