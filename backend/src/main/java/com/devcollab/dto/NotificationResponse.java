package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private String type;
    private String message;
    private UUID relatedEntityId;
    private String link;
    private boolean read;
    private LocalDateTime createdAt;
}
