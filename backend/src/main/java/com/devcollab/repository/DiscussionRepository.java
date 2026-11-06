package com.devcollab.repository;

import com.devcollab.entity.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DiscussionRepository extends JpaRepository<Discussion, UUID> {
    Page<Discussion> findByChannelIdOrderByCreatedAtDesc(UUID channelId, Pageable pageable);

    // Search is a simple case-insensitive LIKE across title/body, scoped to the
    // workspace via the channel relationship. For production scale, this would be
    // replaced with Postgres full-text search (tsvector + GIN index) - noted as a
    // known limitation rather than a hidden one.
    @Query("SELECT d FROM Discussion d WHERE d.channel.workspace.id = :workspaceId "
            + "AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(d.body) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Discussion> searchByWorkspace(UUID workspaceId, String keyword);

    @Query("SELECT COUNT(d) FROM Discussion d WHERE d.channel.workspace.id = :workspaceId")
    long countByWorkspace(UUID workspaceId);
}
