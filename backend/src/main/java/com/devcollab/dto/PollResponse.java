package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PollResponse {
    private UUID id;
    private UUID channelId;
    private String question;
    private boolean closed;
    private List<PollOptionResponse> options;
    private long totalVotes;
    private UUID currentUserVotedOptionId; // null if the caller hasn't voted
    private UUID createdById;
    private String createdByUsername;
    private LocalDateTime createdAt;
}
