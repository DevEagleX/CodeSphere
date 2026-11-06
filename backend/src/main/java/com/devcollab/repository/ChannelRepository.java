package com.devcollab.repository;

import com.devcollab.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<Channel, UUID> {
    boolean existsByWorkspaceIdAndNameIgnoreCase(UUID workspaceId, String name);
    List<Channel> findByWorkspaceIdAndVisibility(UUID workspaceId, Channel.Visibility visibility);
    Optional<Channel> findByIdAndWorkspaceId(UUID id, UUID workspaceId);
    long countByWorkspaceId(UUID workspaceId);
}
