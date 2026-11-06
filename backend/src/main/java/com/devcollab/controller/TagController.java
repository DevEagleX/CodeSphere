package com.devcollab.controller;

import com.devcollab.dto.AddTagRequest;
import com.devcollab.dto.TagResponse;
import com.devcollab.entity.TaggedItem;
import com.devcollab.service.TagService;
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
public class TagController {

    private final TagService tagService;

    @GetMapping("/api/workspaces/{workspaceId}/tags")
    public ResponseEntity<List<TagResponse>> listTags(Authentication auth, @PathVariable UUID workspaceId) {
        return ResponseEntity.ok(tagService.listTags(auth.getName(), workspaceId));
    }

    @GetMapping("/api/workspaces/{workspaceId}/tags/items/{type}/{entityId}")
    public ResponseEntity<List<TagResponse>> itemTags(Authentication auth, @PathVariable UUID workspaceId,
                                                        @PathVariable TaggedItem.EntityType type,
                                                        @PathVariable UUID entityId) {
        return ResponseEntity.ok(tagService.listItemTags(auth.getName(), workspaceId, type, entityId));
    }

    @PostMapping("/api/workspaces/{workspaceId}/discussions/{id}/tags")
    public ResponseEntity<TagResponse> tagDiscussion(Authentication auth, @PathVariable UUID workspaceId,
                                                       @PathVariable UUID id, @Valid @RequestBody AddTagRequest request) {
        TagResponse response = tagService.tagItem(auth.getName(), workspaceId, TaggedItem.EntityType.DISCUSSION, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/workspaces/{workspaceId}/kb/articles/{id}/tags")
    public ResponseEntity<TagResponse> tagArticle(Authentication auth, @PathVariable UUID workspaceId,
                                                    @PathVariable UUID id, @Valid @RequestBody AddTagRequest request) {
        TagResponse response = tagService.tagItem(auth.getName(), workspaceId, TaggedItem.EntityType.KB_ARTICLE, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/workspaces/{workspaceId}/documents/{id}/tags")
    public ResponseEntity<TagResponse> tagDocument(Authentication auth, @PathVariable UUID workspaceId,
                                                     @PathVariable UUID id, @Valid @RequestBody AddTagRequest request) {
        TagResponse response = tagService.tagItem(auth.getName(), workspaceId, TaggedItem.EntityType.DOCUMENT, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
