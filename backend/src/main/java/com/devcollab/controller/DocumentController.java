package com.devcollab.controller;

import com.devcollab.dto.CreateDocumentRequest;
import com.devcollab.dto.DocumentResponse;
import com.devcollab.dto.UpdateDocumentRequest;
import com.devcollab.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/api/workspaces/{workspaceId}/documents")
    public ResponseEntity<DocumentResponse> create(Authentication auth, @PathVariable UUID workspaceId,
                                                     @Valid @RequestBody CreateDocumentRequest request) {
        DocumentResponse response = documentService.create(auth.getName(), workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/workspaces/{workspaceId}/documents")
    public ResponseEntity<List<DocumentResponse>> list(Authentication auth, @PathVariable UUID workspaceId,
                                                         @RequestParam(required = false) UUID parentId) {
        return ResponseEntity.ok(documentService.list(auth.getName(), workspaceId, parentId));
    }

    @GetMapping("/api/documents/{id}")
    public ResponseEntity<DocumentResponse> getOne(Authentication auth, @PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getOne(auth.getName(), id));
    }

    @PutMapping("/api/documents/{id}")
    public ResponseEntity<DocumentResponse> update(Authentication auth, @PathVariable UUID id,
                                                     @Valid @RequestBody UpdateDocumentRequest request) {
        return ResponseEntity.ok(documentService.update(auth.getName(), id, request));
    }

    @DeleteMapping("/api/documents/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable UUID id) {
        documentService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/documents/{id}/versions")
    public ResponseEntity<List<com.devcollab.dto.DocumentVersionResponse>> listVersions(Authentication auth,
                                                                                          @PathVariable UUID id) {
        return ResponseEntity.ok(documentService.listVersions(auth.getName(), id));
    }

    @PostMapping("/api/documents/{id}/versions/{versionNumber}/restore")
    public ResponseEntity<DocumentResponse> restoreVersion(Authentication auth, @PathVariable UUID id,
                                                             @PathVariable int versionNumber) {
        return ResponseEntity.ok(documentService.restoreVersion(auth.getName(), id, versionNumber));
    }
}
