package com.devcollab.service;

import com.devcollab.dto.CreateReplyRequest;
import com.devcollab.dto.ReplyResponse;
import com.devcollab.dto.UpdateReplyRequest;
import com.devcollab.entity.Discussion;
import com.devcollab.entity.Reply;
import com.devcollab.entity.User;
import com.devcollab.entity.WorkspaceMember;
import com.devcollab.exception.ForbiddenOperationException;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.mapper.ReplyMapper;
import com.devcollab.repository.ReplyRepository;
import com.devcollab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final DiscussionService discussionService;
    private final ChannelService channelService;
    private final WorkspaceService workspaceService;
    private final NotificationService notificationService;
    private final ReplyMapper replyMapper;

    @Transactional
    public ReplyResponse create(String username, UUID discussionId, CreateReplyRequest request) {
        Discussion discussion = discussionService.findAndAuthorizeRead(username, discussionId);
        User author = findUser(username);

        Reply parent = null;
        if (request.getParentReplyId() != null) {
            parent = replyRepository.findById(request.getParentReplyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent reply not found: " + request.getParentReplyId()));

            if (!parent.getDiscussion().getId().equals(discussionId)) {
                throw new ResourceNotFoundException("Parent reply does not belong to this discussion");
            }
        }

        Reply reply = Reply.builder()
                .discussion(discussion)
                .parentReply(parent)
                .author(author)
                .body(request.getBody())
                .build();
        replyRepository.save(reply);

        String link = "/w/" + discussion.getChannel().getWorkspace().getId() + "/discussions/" + discussionId;
        notificationService.notifyMentions(request.getBody(), username, reply.getId(), link);

        // Notify whoever this reply is actually responding to: the parent reply's
        // author if nested, otherwise the discussion's author for a top-level reply.
        // Mentions are handled separately above and could overlap with this - that's
        // fine, a person can reasonably get both notifications for different reasons.
        User notifyTarget = (parent != null) ? parent.getAuthor() : discussion.getAuthor();
        if (!notifyTarget.getUsername().equals(username)) {
            notificationService.notify(
                    notifyTarget,
                    com.devcollab.entity.Notification.NotificationType.REPLY,
                    "@" + username + " replied to your " + (parent != null ? "reply" : "discussion"),
                    reply.getId(),
                    link
            );
        }

        return replyMapper.toFlatResponse(reply);
    }

    public List<ReplyResponse> getTree(String username, UUID discussionId) {
        discussionService.findAndAuthorizeRead(username, discussionId);
        List<Reply> flat = replyRepository.findByDiscussionIdOrderByCreatedAtAsc(discussionId);
        return replyMapper.toTree(flat);
    }

    @Transactional
    public ReplyResponse update(String username, UUID replyId, UpdateReplyRequest request) {
        Reply reply = findReply(replyId);
        discussionService.findAndAuthorizeRead(username, reply.getDiscussion().getId());

        if (!reply.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenOperationException("Only the author can edit this reply");
        }

        reply.setBody(request.getBody());
        replyRepository.save(reply);

        return replyMapper.toFlatResponse(reply);
    }

    @Transactional
    public void delete(String username, UUID replyId) {
        Reply reply = findReply(replyId);
        UUID workspaceId = reply.getDiscussion().getChannel().getWorkspace().getId();
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);

        boolean isAuthor = reply.getAuthor().getUsername().equals(username);
        boolean isAdminOrOwner = membership.getRole() == WorkspaceMember.WorkspaceRole.OWNER
                || membership.getRole() == WorkspaceMember.WorkspaceRole.ADMIN;

        if (!isAuthor && !isAdminOrOwner) {
            throw new ForbiddenOperationException("Only the author or a workspace admin/owner can delete this reply");
        }

        replyRepository.delete(reply);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Reply findReply(UUID id) {
        return replyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reply not found: " + id));
    }
}
