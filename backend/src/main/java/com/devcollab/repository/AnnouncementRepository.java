package com.devcollab.repository;

import com.devcollab.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    // Pinned announcements surface first, then newest first within each group.
    List<Announcement> findByWorkspaceIdOrderByPinnedDescCreatedAtDesc(UUID workspaceId);
    Optional<Announcement> findByIdAndWorkspaceId(UUID id, UUID workspaceId);
}
