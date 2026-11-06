package com.devcollab.service;

import com.devcollab.dto.WorkspaceAnalyticsResponse;
import com.devcollab.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Read-only aggregate counts per workspace. Deliberately implemented as simple
 * COUNT(*) queries fired in sequence rather than a single mega-join or a
 * pre-computed materialized view - at this app's scale, N cheap indexed count
 * queries are simpler to read and maintain than a denormalized rollup table, and
 * every count here is already backed by an index on workspace_id (see each
 * feature's Database Design notes). If this endpoint became a hot path under
 * heavy load, a scheduled job writing to a WorkspaceStats table would be the
 * next step - not needed yet.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final WorkspaceService workspaceService;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelRepository channelRepository;
    private final DiscussionRepository discussionRepository;
    private final ReplyRepository replyRepository;
    private final KbArticleRepository kbArticleRepository;
    private final DocumentRepository documentRepository;
    private final ResourceRepository resourceRepository;

    public WorkspaceAnalyticsResponse getWorkspaceAnalytics(String username, UUID workspaceId) {
        workspaceService.requireMembership(workspaceId, username);

        return WorkspaceAnalyticsResponse.builder()
                .workspaceId(workspaceId)
                .memberCount(workspaceMemberRepository.countByWorkspaceId(workspaceId))
                .channelCount(channelRepository.countByWorkspaceId(workspaceId))
                .discussionCount(discussionRepository.countByWorkspace(workspaceId))
                .replyCount(replyRepository.countByWorkspace(workspaceId))
                .kbArticleCount(kbArticleRepository.countByWorkspaceId(workspaceId))
                .documentCount(documentRepository.countByWorkspaceId(workspaceId))
                .resourceCount(resourceRepository.countByWorkspaceId(workspaceId))
                .build();
    }
}
