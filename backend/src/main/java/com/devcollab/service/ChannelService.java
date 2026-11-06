package com.devcollab.service;

import com.devcollab.dto.AddChannelMemberRequest;
import com.devcollab.dto.ChannelMemberResponse;
import com.devcollab.dto.ChannelResponse;
import com.devcollab.dto.CreateChannelRequest;
import com.devcollab.entity.*;
import com.devcollab.exception.DuplicateResourceException;
import com.devcollab.exception.ForbiddenOperationException;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.mapper.ChannelMapper;
import com.devcollab.repository.ChannelMemberRepository;
import com.devcollab.repository.ChannelRepository;
import com.devcollab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final ActivityService activityService;
    private final ChannelMapper channelMapper;

    @Transactional
    public ChannelResponse createChannel(String username, UUID workspaceId, CreateChannelRequest request) {
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);

        if (channelRepository.existsByWorkspaceIdAndNameIgnoreCase(workspaceId, request.getName())) {
            throw new DuplicateResourceException("A channel named '" + request.getName() + "' already exists in this workspace");
        }

        Channel channel = Channel.builder()
                .workspace(membership.getWorkspace())
                .name(request.getName())
                .description(request.getDescription())
                .visibility(request.getVisibility())
                .createdBy(membership.getUser())
                .build();
        channelRepository.save(channel);

        activityService.log(membership.getWorkspace(), membership.getUser(),
                com.devcollab.entity.ActivityEvent.ActivityType.CHANNEL_CREATED,
                membership.getUser().getUsername() + " created channel #" + channel.getName());

        long memberCount = 0;
        if (channel.getVisibility() == Channel.Visibility.PRIVATE) {
            ChannelMember creatorMembership = ChannelMember.builder()
                    .channel(channel)
                    .user(membership.getUser())
                    .build();
            channelMemberRepository.save(creatorMembership);
            memberCount = 1;
        }

        return channelMapper.toChannelResponse(channel, memberCount);
    }

    public List<ChannelResponse> listChannels(String username, UUID workspaceId) {
        User user = findUser(username);
        workspaceService.requireMembership(workspaceId, username);

        List<Channel> visible = new ArrayList<>(
                channelRepository.findByWorkspaceIdAndVisibility(workspaceId, Channel.Visibility.PUBLIC));

        List<Channel> privateChannels = channelRepository.findByWorkspaceIdAndVisibility(workspaceId, Channel.Visibility.PRIVATE);
        for (Channel channel : privateChannels) {
            if (channelMemberRepository.existsByChannelIdAndUserId(channel.getId(), user.getId())) {
                visible.add(channel);
            }
        }

        return visible.stream()
                .map(c -> channelMapper.toChannelResponse(c, channelMemberRepository.countByChannelId(c.getId())))
                .toList();
    }

    public ChannelResponse getChannel(String username, UUID workspaceId, UUID channelId) {
        Channel channel = assertReadableChannel(username, workspaceId, channelId);
        long memberCount = channelMemberRepository.countByChannelId(channelId);
        return channelMapper.toChannelResponse(channel, memberCount);
    }

    // Package-private so other services (DiscussionService) can reuse this exact
    // read-access rule instead of re-implementing the 404-vs-403 information-hiding logic.
    Channel assertReadableChannel(String username, UUID workspaceId, UUID channelId) {
        User user = findUser(username);
        workspaceService.requireMembership(workspaceId, username);

        Channel channel = findChannelInWorkspace(channelId, workspaceId);

        if (channel.getVisibility() == Channel.Visibility.PRIVATE
                && !channelMemberRepository.existsByChannelIdAndUserId(channelId, user.getId())) {
            throw new ResourceNotFoundException("Channel not found: " + channelId);
        }

        return channel;
    }

    @Transactional
    public ChannelMemberResponse addMember(String requesterUsername, UUID workspaceId, UUID channelId,
                                            AddChannelMemberRequest request) {
        User requester = findUser(requesterUsername);
        workspaceService.requireMembership(workspaceId, requesterUsername);

        Channel channel = findChannelInWorkspace(channelId, workspaceId);

        if (channel.getVisibility() != Channel.Visibility.PRIVATE) {
            throw new ForbiddenOperationException("Only private channels have explicit membership");
        }
        if (!channelMemberRepository.existsByChannelIdAndUserId(channelId, requester.getId())) {
            throw new ForbiddenOperationException("You must be a member of this channel to add others");
        }

        User userToAdd = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUsername()));

        workspaceService.requireMembership(workspaceId, userToAdd.getUsername());

        if (channelMemberRepository.existsByChannelIdAndUserId(channelId, userToAdd.getId())) {
            throw new DuplicateResourceException("User is already a member of this channel");
        }

        ChannelMember newMember = ChannelMember.builder()
                .channel(channel)
                .user(userToAdd)
                .build();
        channelMemberRepository.save(newMember);

        return channelMapper.toMemberResponse(newMember);
    }

    @Transactional
    public void deleteChannel(String username, UUID workspaceId, UUID channelId) {
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);
        Channel channel = findChannelInWorkspace(channelId, workspaceId);

        boolean isCreator = channel.getCreatedBy().getId().equals(membership.getUser().getId());
        boolean isWorkspaceAdminOrOwner = membership.getRole() == WorkspaceMember.WorkspaceRole.OWNER
                || membership.getRole() == WorkspaceMember.WorkspaceRole.ADMIN;

        if (!isCreator && !isWorkspaceAdminOrOwner) {
            throw new ForbiddenOperationException("Only the channel creator or a workspace admin/owner can delete this channel");
        }

        channelRepository.delete(channel);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Channel findChannelInWorkspace(UUID channelId, UUID workspaceId) {
        return channelRepository.findByIdAndWorkspaceId(channelId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found: " + channelId));
    }
}
