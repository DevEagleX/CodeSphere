package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class WorkspaceMemberResponse {
    private UUID userId;
    private String username;
    private String role;
    private LocalDateTime joinedAt;
}
