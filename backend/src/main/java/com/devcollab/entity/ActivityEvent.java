package com.devcollab.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

// A write-once, read-many log of "something happened" events, scoped to a
// workspace. Deliberately denormalized (stores a pre-rendered description string
// rather than requiring joins to reconstruct "what happened" later) since activity
// feeds are read far more often than written and the source entity may itself be
// deleted later (e.g. a discussion gets deleted, but the feed should still show
// "X created a discussion" as history).
@Entity
@Table(name = "activity_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityType type;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ActivityType {
        WORKSPACE_CREATED, CHANNEL_CREATED, DISCUSSION_CREATED,
        ARTICLE_CREATED, DOCUMENT_CREATED, MEMBER_ADDED
    }
}
