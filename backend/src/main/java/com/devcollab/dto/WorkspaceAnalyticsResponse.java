package com.devcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class WorkspaceAnalyticsResponse {
    private UUID workspaceId;
    private long memberCount;
    private long channelCount;
    private long discussionCount;
    private long replyCount;
    private long kbArticleCount;
    private long documentCount;
    private long resourceCount;
}
