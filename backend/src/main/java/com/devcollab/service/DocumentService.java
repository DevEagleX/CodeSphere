package com.devcollab.service;

import com.devcollab.dto.CreateDocumentRequest;
import com.devcollab.dto.DocumentResponse;
import com.devcollab.dto.DocumentVersionResponse;
import com.devcollab.dto.UpdateDocumentRequest;
import com.devcollab.entity.Document;
import com.devcollab.entity.DocumentVersion;
import com.devcollab.entity.User;
import com.devcollab.entity.WorkspaceMember;
import com.devcollab.exception.ForbiddenOperationException;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.mapper.DocumentMapper;
import com.devcollab.repository.DocumentRepository;
import com.devcollab.repository.DocumentVersionRepository;
import com.devcollab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final ActivityService activityService;
    private final DocumentMapper documentMapper;

    @Transactional
    public DocumentResponse create(String username, UUID workspaceId, CreateDocumentRequest request) {
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);
        User author = findUser(username);

        Document parent = null;
        if (request.getParentDocumentId() != null) {
            parent = documentRepository.findByIdAndWorkspaceId(request.getParentDocumentId(), workspaceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent document not found: " + request.getParentDocumentId()));
        }

        Document document = Document.builder()
                .workspace(membership.getWorkspace())
                .parentDocument(parent)
                .createdBy(author)
                .lastEditedBy(author)
                .title(request.getTitle())
                .body(request.getBody() != null ? request.getBody() : "")
                .build();
        documentRepository.save(document);

        activityService.log(membership.getWorkspace(), author,
                com.devcollab.entity.ActivityEvent.ActivityType.DOCUMENT_CREATED,
                author.getUsername() + " created a document: " + document.getTitle());

        return documentMapper.toResponse(document);
    }

    public List<DocumentResponse> list(String username, UUID workspaceId, UUID parentId) {
        workspaceService.requireMembership(workspaceId, username);

        List<Document> documents = (parentId != null)
                ? documentRepository.findByWorkspaceIdAndParentDocumentId(workspaceId, parentId)
                : documentRepository.findByWorkspaceIdAndParentDocumentIdIsNull(workspaceId);

        return documents.stream().map(documentMapper::toResponse).toList();
    }

    public DocumentResponse getOne(String username, UUID documentId) {
        Document document = findDocument(documentId);
        workspaceService.requireMembership(document.getWorkspace().getId(), username);
        return documentMapper.toResponse(document);
    }

    @Transactional
    public DocumentResponse update(String username, UUID documentId, UpdateDocumentRequest request) {
        Document document = findDocument(documentId);
        workspaceService.requireMembership(document.getWorkspace().getId(), username);
        User editor = findUser(username);

        // --- Feature 11 (Version History) ---
        // Snapshot the pre-edit state before overwriting, inside this same transaction,
        // so a version and its edit are always consistent (both commit or both roll back).
        DocumentVersion snapshot = DocumentVersion.builder()
                .document(document)
                .editedBy(editor)
                .title(document.getTitle())
                .body(document.getBody())
                .versionNumber(document.getVersionNumber())
                .build();
        documentVersionRepository.save(snapshot);

        document.setTitle(request.getTitle());
        document.setBody(request.getBody() != null ? request.getBody() : "");
        document.setLastEditedBy(editor);
        document.setVersionNumber(document.getVersionNumber() + 1);
        documentRepository.save(document);

        return documentMapper.toResponse(document);
    }

    public List<DocumentVersionResponse> listVersions(String username, UUID documentId) {
        Document document = findDocument(documentId);
        workspaceService.requireMembership(document.getWorkspace().getId(), username);

        return documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId).stream()
                .map(v -> DocumentVersionResponse.builder()
                        .id(v.getId())
                        .versionNumber(v.getVersionNumber())
                        .title(v.getTitle())
                        .body(v.getBody())
                        .editedById(v.getEditedBy().getId())
                        .editedByUsername(v.getEditedBy().getUsername())
                        .createdAt(v.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public DocumentResponse restoreVersion(String username, UUID documentId, int versionNumber) {
        Document document = findDocument(documentId);
        workspaceService.requireMembership(document.getWorkspace().getId(), username);
        User editor = findUser(username);

        DocumentVersion versionToRestore = documentVersionRepository
                .findByDocumentIdAndVersionNumber(documentId, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Version " + versionNumber + " not found for this document"));

        // Restoring is itself an edit: snapshot the current state first (so restoring
        // is undoable too), then apply the old content as a new version.
        DocumentVersion snapshot = DocumentVersion.builder()
                .document(document)
                .editedBy(editor)
                .title(document.getTitle())
                .body(document.getBody())
                .versionNumber(document.getVersionNumber())
                .build();
        documentVersionRepository.save(snapshot);

        document.setTitle(versionToRestore.getTitle());
        document.setBody(versionToRestore.getBody());
        document.setLastEditedBy(editor);
        document.setVersionNumber(document.getVersionNumber() + 1);
        documentRepository.save(document);

        return documentMapper.toResponse(document);
    }

    @Transactional
    public void delete(String username, UUID documentId) {
        Document document = findDocument(documentId);
        UUID workspaceId = document.getWorkspace().getId();
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);

        boolean isCreator = document.getCreatedBy().getUsername().equals(username);
        boolean isAdminOrOwner = membership.getRole() == WorkspaceMember.WorkspaceRole.OWNER
                || membership.getRole() == WorkspaceMember.WorkspaceRole.ADMIN;

        if (!isCreator && !isAdminOrOwner) {
            throw new ForbiddenOperationException("Only the creator or a workspace admin/owner can delete this document");
        }

        documentRepository.delete(document); // cascades to nested child documents
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Document findDocument(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
    }
}
