package com.devcollab.controller;

import com.devcollab.dto.CreateReplyRequest;
import com.devcollab.dto.ReplyResponse;
import com.devcollab.dto.UpdateReplyRequest;
import com.devcollab.service.ReplyService;
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
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/api/discussions/{discussionId}/replies")
    public ResponseEntity<ReplyResponse> create(Authentication auth, @PathVariable UUID discussionId,
                                                 @Valid @RequestBody CreateReplyRequest request) {
        ReplyResponse response = replyService.create(auth.getName(), discussionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/discussions/{discussionId}/replies")
    public ResponseEntity<List<ReplyResponse>> getTree(Authentication auth, @PathVariable UUID discussionId) {
        return ResponseEntity.ok(replyService.getTree(auth.getName(), discussionId));
    }

    @PutMapping("/api/replies/{id}")
    public ResponseEntity<ReplyResponse> update(Authentication auth, @PathVariable UUID id,
                                                 @Valid @RequestBody UpdateReplyRequest request) {
        return ResponseEntity.ok(replyService.update(auth.getName(), id, request));
    }

    @DeleteMapping("/api/replies/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable UUID id) {
        replyService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
