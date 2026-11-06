package com.devcollab.service;

import com.devcollab.dto.*;
import com.devcollab.entity.Channel;
import com.devcollab.entity.Discussion;
import com.devcollab.entity.User;
import com.devcollab.entity.WorkspaceMember;
import com.devcollab.exception.ForbiddenOperationException;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.mapper.DiscussionMapper;
import com.devcollab.repository.DiscussionRepository;
import com.devcollab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final UserRepository userRepository;
    private final ChannelService channelService;
    private final WorkspaceService workspaceService;
    private final NotificationService notificationService;
    private final ActivityService activityService;
    private final DiscussionMapper discussionMapper;

    @Transactional
    public DiscussionResponse create(String username, UUID workspaceId, UUID channelId,
                                      CreateDiscussionRequest request) {
        Channel channel = channelService.assertReadableChannel(username, workspaceId, channelId);
        User author = findUser(username);

        Discussion discussion = Discussion.builder()
                .channel(channel)
                .author(author)
                .title(request.getTitle())
                .body(request.getBody())
                .build();
        discussionRepository.save(discussion);

        notificationService.notifyMentions(request.getBody(), username, discussion.getId(),
                "/w/" + workspaceId + "/discussions/" + discussion.getId());
        activityService.log(channel.getWorkspace(), author,
                com.devcollab.entity.ActivityEvent.ActivityType.DISCUSSION_CREATED,
                author.getUsername() + " started a discussion: " + discussion.getTitle());

        return discussionMapper.toResponse(discussion);
    }

    public PageResponse<DiscussionResponse> list(String username, UUID workspaceId, UUID channelId, Pageable pageable) {
        channelService.assertReadableChannel(username, workspaceId, channelId);

        Page<Discussion> page = discussionRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageable);
        return PageResponse.from(page, discussionMapper::toResponse);
    }

    public DiscussionResponse getOne(String username, UUID discussionId) {
        Discussion discussion = findAndAuthorizeRead(username, discussionId);
        return discussionMapper.toResponse(discussion);
    }

    // Package-private so ReplyService can reuse the exact same "does this caller have
    // read access to this discussion's channel" check instead of duplicating it.
    Discussion findAndAuthorizeRead(String username, UUID discussionId) {
        Discussion discussion = findDiscussion(discussionId);
        channelService.assertReadableChannel(username, discussion.getChannel().getWorkspace().getId(),
                discussion.getChannel().getId());
        return discussion;
    }

    @Transactional
    public DiscussionResponse update(String username, UUID discussionId, UpdateDiscussionRequest request) {
        Discussion discussion = findDiscussion(discussionId);
        channelService.assertReadableChannel(username, discussion.getChannel().getWorkspace().getId(),
                discussion.getChannel().getId());

        if (!discussion.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenOperationException("Only the author can edit this discussion");
        }

        discussion.setTitle(request.getTitle());
        discussion.setBody(request.getBody());
        discussionRepository.save(discussion);

        return discussionMapper.toResponse(discussion);
    }

    @Transactional
    public void delete(String username, UUID discussionId) {
        Discussion discussion = findDiscussion(discussionId);
        UUID workspaceId = discussion.getChannel().getWorkspace().getId();
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);

        boolean isAuthor = discussion.getAuthor().getUsername().equals(username);
        boolean isAdminOrOwner = membership.getRole() == WorkspaceMember.WorkspaceRole.OWNER
                || membership.getRole() == WorkspaceMember.WorkspaceRole.ADMIN;

        if (!isAuthor && !isAdminOrOwner) {
            throw new ForbiddenOperationException("Only the author or a workspace admin/owner can delete this discussion");
        }

        discussionRepository.delete(discussion);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Discussion findDiscussion(UUID id) {
        return discussionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found: " + id));
    }
}
