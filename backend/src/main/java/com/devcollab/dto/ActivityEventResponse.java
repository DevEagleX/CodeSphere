package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ActivityEventResponse {
    private UUID id;
    private String type;
    private String description;
    private UUID actorId;
    private String actorUsername;
    private LocalDateTime createdAt;
}
