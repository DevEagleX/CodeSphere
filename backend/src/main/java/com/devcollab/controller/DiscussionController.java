package com.devcollab.controller;

import com.devcollab.dto.*;
import com.devcollab.service.DiscussionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    @PostMapping("/api/workspaces/{workspaceId}/channels/{channelId}/discussions")
    public ResponseEntity<DiscussionResponse> create(Authentication auth,
                                                       @PathVariable UUID workspaceId,
                                                       @PathVariable UUID channelId,
                                                       @Valid @RequestBody CreateDiscussionRequest request) {
        DiscussionResponse response = discussionService.create(auth.getName(), workspaceId, channelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/workspaces/{workspaceId}/channels/{channelId}/discussions")
    public ResponseEntity<PageResponse<DiscussionResponse>> list(Authentication auth,
                                                                   @PathVariable UUID workspaceId,
                                                                   @PathVariable UUID channelId,
                                                                   @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(discussionService.list(auth.getName(), workspaceId, channelId, pageable));
    }

    @GetMapping("/api/discussions/{id}")
    public ResponseEntity<DiscussionResponse> getOne(Authentication auth, @PathVariable UUID id) {
        return ResponseEntity.ok(discussionService.getOne(auth.getName(), id));
    }

    @PutMapping("/api/discussions/{id}")
    public ResponseEntity<DiscussionResponse> update(Authentication auth, @PathVariable UUID id,
                                                       @Valid @RequestBody UpdateDiscussionRequest request) {
        return ResponseEntity.ok(discussionService.update(auth.getName(), id, request));
    }

    @DeleteMapping("/api/discussions/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable UUID id) {
        discussionService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
