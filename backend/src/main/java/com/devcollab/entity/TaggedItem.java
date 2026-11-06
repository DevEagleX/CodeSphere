package com.devcollab.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

// Polymorphic association: (tagId, entityType, entityId) instead of a separate
// join table per taggable type (discussion_tags, article_tags, document_tags...).
// Trade-off made explicitly: we lose a DB-level FK constraint on entityId (the DB
// can't verify a Discussion with that ID actually exists), in exchange for adding
// new taggable types later without a schema migration or a new join table. Given
// this is a portfolio-scale app with three taggable types today and more likely
// later (polls, KB articles already exist, more may come), the flexibility wins.
@Entity
@Table(name = "tagged_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tag_id", "entity_type", "entity_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaggedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tag tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum EntityType {
        DISCUSSION, KB_ARTICLE, DOCUMENT
    }
}
