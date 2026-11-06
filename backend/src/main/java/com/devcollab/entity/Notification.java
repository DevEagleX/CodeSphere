package com.devcollab.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The recipient. No FK cascade concerns here since notifications are purely
    // informational and disappearing along with a deleted user account is fine
    // (default JPA behavior - no @OnDelete needed).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    // Loosely-typed reference to whatever triggered this notification (a discussion,
    // reply, mention, etc.) - same polymorphic-by-convention approach as TaggedItem's
    // entityId, but without a formal entityType enum since notifications are read-only
    // display data, not something the app needs to query "all notifications about X".
    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    // Frontend route to open when the notification is clicked, e.g. /w/{workspaceId}/discussions/{id}.
    // Nullable so notifications created before this column existed still load.
    @Column(length = 300)
    private String link;

    @Column(nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        MENTION, REPLY, WORKSPACE_INVITE
    }
}
