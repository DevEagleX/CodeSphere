package com.devcollab.repository;

import com.devcollab.entity.TaggedItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaggedItemRepository extends JpaRepository<TaggedItem, UUID> {
    List<TaggedItem> findByEntityTypeAndEntityId(TaggedItem.EntityType entityType, UUID entityId);
    List<TaggedItem> findByTagId(UUID tagId);
    boolean existsByTagIdAndEntityTypeAndEntityId(UUID tagId, TaggedItem.EntityType entityType, UUID entityId);
}
