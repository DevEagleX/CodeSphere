package com.devcollab.repository;

import com.devcollab.entity.KbCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KbCategoryRepository extends JpaRepository<KbCategory, UUID> {
    List<KbCategory> findByWorkspaceId(UUID workspaceId);
    boolean existsByWorkspaceIdAndSlug(UUID workspaceId, String slug);
    Optional<KbCategory> findByIdAndWorkspaceId(UUID id, UUID workspaceId);
}
