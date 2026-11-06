package com.devcollab.mapper;

import com.devcollab.dto.DocumentResponse;
import com.devcollab.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .workspaceId(document.getWorkspace().getId())
                .parentDocumentId(document.getParentDocument() != null ? document.getParentDocument().getId() : null)
                .createdById(document.getCreatedBy().getId())
                .createdByUsername(document.getCreatedBy().getUsername())
                .lastEditedById(document.getLastEditedBy().getId())
                .lastEditedByUsername(document.getLastEditedBy().getUsername())
                .title(document.getTitle())
                .body(document.getBody())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
