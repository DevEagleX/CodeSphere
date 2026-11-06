package com.devcollab.service;

import com.devcollab.dto.*;
import com.devcollab.entity.*;
import com.devcollab.exception.DuplicateResourceException;
import com.devcollab.exception.ForbiddenOperationException;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.repository.PollRepository;
import com.devcollab.repository.PollVoteRepository;
import com.devcollab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;
    private final UserRepository userRepository;
    private final ChannelService channelService;
    private final WorkspaceService workspaceService;

    @Transactional
    public PollResponse create(String username, UUID workspaceId, UUID channelId, CreatePollRequest request) {
        Channel channel = channelService.assertReadableChannel(username, workspaceId, channelId);
        User creator = findUser(username);

        Poll poll = Poll.builder()
                .channel(channel)
                .createdBy(creator)
                .question(request.getQuestion())
                .build();

        for (String optionText : request.getOptions()) {
            poll.getOptions().add(PollOption.builder().poll(poll).text(optionText).build());
        }

        pollRepository.save(poll); // cascades to PollOption rows via CascadeType.ALL

        return toResponse(poll, username);
    }

    public List<PollResponse> list(String username, UUID workspaceId, UUID channelId) {
        channelService.assertReadableChannel(username, workspaceId, channelId);
        return pollRepository.findByChannelIdOrderByCreatedAtDesc(channelId).stream()
                .map(p -> toResponse(p, username))
                .toList();
    }

    @Transactional
    public PollResponse vote(String username, UUID pollId, UUID optionId) {
        Poll poll = findPoll(pollId);
        User voter = findUser(username);
        channelService.assertReadableChannel(username, poll.getChannel().getWorkspace().getId(), poll.getChannel().getId());

        if (poll.isClosed()) {
            throw new ForbiddenOperationException("This poll is closed");
        }

        PollOption option = poll.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Poll option not found: " + optionId));

        if (pollVoteRepository.findByPollIdAndUserId(pollId, voter.getId()).isPresent()) {
            throw new DuplicateResourceException("You have already voted on this poll");
        }

        PollVote vote = PollVote.builder()
                .poll(poll)
                .pollOption(option)
                .user(voter)
                .build();
        pollVoteRepository.save(vote);

        return toResponse(poll, username);
    }

    @Transactional
    public void close(String username, UUID pollId) {
        Poll poll = findPoll(pollId);
        UUID workspaceId = poll.getChannel().getWorkspace().getId();
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);

        boolean isCreator = poll.getCreatedBy().getUsername().equals(username);
        boolean isAdminOrOwner = membership.getRole() == WorkspaceMember.WorkspaceRole.OWNER
                || membership.getRole() == WorkspaceMember.WorkspaceRole.ADMIN;

        if (!isCreator && !isAdminOrOwner) {
            throw new ForbiddenOperationException("Only the poll creator or a workspace admin/owner can close this poll");
        }

        poll.setClosed(true);
        pollRepository.save(poll);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Poll findPoll(UUID id) {
        return pollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found: " + id));
    }

    private PollResponse toResponse(Poll poll, String requestingUsername) {
        List<PollVote> allVotes = pollVoteRepository.findByPollId(poll.getId());

        List<PollOptionResponse> optionResponses = poll.getOptions().stream()
                .map(o -> PollOptionResponse.builder()
                        .id(o.getId())
                        .text(o.getText())
                        .voteCount(pollVoteRepository.countByPollOptionId(o.getId()))
                        .build())
                .toList();

        UUID currentUserVotedOptionId = allVotes.stream()
                .filter(v -> v.getUser().getUsername().equals(requestingUsername))
                .map(v -> v.getPollOption().getId())
                .findFirst()
                .orElse(null);

        return PollResponse.builder()
                .id(poll.getId())
                .channelId(poll.getChannel().getId())
                .question(poll.getQuestion())
                .closed(poll.isClosed())
                .options(optionResponses)
                .totalVotes(allVotes.size())
                .currentUserVotedOptionId(currentUserVotedOptionId)
                .createdById(poll.getCreatedBy().getId())
                .createdByUsername(poll.getCreatedBy().getUsername())
                .createdAt(poll.getCreatedAt())
                .build();
    }
}
