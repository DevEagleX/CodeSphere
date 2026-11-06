package com.devcollab.repository;

import com.devcollab.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByWorkspaceIdAndParentDocumentIdIsNull(UUID workspaceId);
    List<Document> findByWorkspaceIdAndParentDocumentId(UUID workspaceId, UUID parentDocumentId);
    Optional<Document> findByIdAndWorkspaceId(UUID id, UUID workspaceId);

    @Query("SELECT d FROM Document d WHERE d.workspace.id = :workspaceId "
            + "AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(d.body) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Document> searchByWorkspace(UUID workspaceId, String keyword);
    long countByWorkspaceId(UUID workspaceId);
}
