package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReplyResponse {
    private UUID id;
    private UUID discussionId;
    private UUID parentReplyId;
    private UUID authorId;
    private String authorUsername;
    private String body;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<ReplyResponse> children = new ArrayList<>();
}
