package com.devcollab.repository;

import com.devcollab.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReplyRepository extends JpaRepository<Reply, UUID> {
    List<Reply> findByDiscussionIdOrderByCreatedAtAsc(UUID discussionId);

    @Query("SELECT COUNT(r) FROM Reply r WHERE r.discussion.channel.workspace.id = :workspaceId")
    long countByWorkspace(UUID workspaceId);
}
