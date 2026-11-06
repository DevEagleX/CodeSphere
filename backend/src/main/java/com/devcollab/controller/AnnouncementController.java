package com.devcollab.controller;

import com.devcollab.dto.AnnouncementResponse;
import com.devcollab.dto.CreateAnnouncementRequest;
import com.devcollab.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    public ResponseEntity<AnnouncementResponse> create(Authentication auth, @PathVariable UUID workspaceId,
                                                         @Valid @RequestBody CreateAnnouncementRequest request) {
        AnnouncementResponse response = announcementService.create(auth.getName(), workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> list(Authentication auth, @PathVariable UUID workspaceId) {
        return ResponseEntity.ok(announcementService.list(auth.getName(), workspaceId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable UUID workspaceId, @PathVariable UUID id) {
        announcementService.delete(auth.getName(), workspaceId, id);
        return ResponseEntity.noContent().build();
    }
}
