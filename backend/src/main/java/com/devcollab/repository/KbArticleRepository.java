package com.devcollab.repository;

import com.devcollab.entity.KbArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KbArticleRepository extends JpaRepository<KbArticle, UUID> {
    boolean existsByWorkspaceIdAndSlug(UUID workspaceId, String slug);
    Optional<KbArticle> findByIdAndWorkspaceId(UUID id, UUID workspaceId);
    Page<KbArticle> findByWorkspaceIdAndPublishedTrue(UUID workspaceId, Pageable pageable);
    Page<KbArticle> findByWorkspaceIdAndCategoryIdAndPublishedTrue(UUID workspaceId, UUID categoryId, Pageable pageable);
    List<KbArticle> findByCategoryId(UUID categoryId);

    @Query("SELECT a FROM KbArticle a WHERE a.workspace.id = :workspaceId AND a.published = true "
            + "AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(a.body) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<KbArticle> searchByWorkspace(UUID workspaceId, String keyword);
    long countByWorkspaceId(UUID workspaceId);
}
