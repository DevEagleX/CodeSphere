package com.devcollab.service;

import com.devcollab.dto.NotificationResponse;
import com.devcollab.dto.PageResponse;
import com.devcollab.entity.Notification;
import com.devcollab.entity.User;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.repository.NotificationRepository;
import com.devcollab.repository.UserRepository;
import com.devcollab.util.MentionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // --- Creation (called internally by other services - never exposed as a public API) ---

    @Transactional
    public void notify(User recipient, Notification.NotificationType type, String message, UUID relatedEntityId,
                       String link) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .link(link)
                .build();
        notificationRepository.save(notification);
    }

    /**
     * Scans free-text content (a discussion/reply body) for @username mentions and
     * notifies each mentioned user who actually exists, excluding the author
     * themselves (no self-notifications). Silently ignores @handles that don't
     * match a real username - this is intentionally lenient rather than erroring,
     * since "@username" in a code block or prose isn't necessarily a real mention.
     */
    @Transactional
    public void notifyMentions(String text, String authorUsername, UUID relatedEntityId, String link) {
        Set<String> mentioned = MentionUtil.extractMentionedUsernames(text);

        for (String username : mentioned) {
            if (username.equalsIgnoreCase(authorUsername)) continue;

            userRepository.findByUsername(username).ifPresent(user -> notify(
                    user,
                    Notification.NotificationType.MENTION,
                    "@" + authorUsername + " mentioned you",
                    relatedEntityId,
                    link
            ));
        }
    }

    // --- Reading (the actual public API) ---

    public PageResponse<NotificationResponse> listMine(String username, Pageable pageable) {
        User user = findUser(username);
        Page<Notification> page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable);
        return PageResponse.from(page, this::toResponse);
    }

    public long unreadCount(String username) {
        User user = findUser(username);
        return notificationRepository.countByRecipientIdAndReadFalse(user.getId());
    }

    @Transactional
    public void markRead(String username, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (!notification.getRecipient().getUsername().equals(username)) {
            throw new ResourceNotFoundException("Notification not found: " + notificationId); // don't leak existence
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .message(n.getMessage())
                .relatedEntityId(n.getRelatedEntityId())
                .link(n.getLink())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
