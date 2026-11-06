package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ChannelMemberResponse {
    private UUID userId;
    private String username;
    private LocalDateTime joinedAt;
}
