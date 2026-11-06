package com.devcollab.controller;

import com.devcollab.dto.*;
import com.devcollab.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(Authentication auth,
                                                      @Valid @RequestBody CreateWorkspaceRequest request) {
        WorkspaceResponse response = workspaceService.createWorkspace(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> listMine(Authentication auth) {
        return ResponseEntity.ok(workspaceService.listMyWorkspaces(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> getOne(Authentication auth, @PathVariable UUID id) {
        return ResponseEntity.ok(workspaceService.getWorkspace(auth.getName(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> update(Authentication auth, @PathVariable UUID id,
                                                      @Valid @RequestBody UpdateWorkspaceRequest request) {
        return ResponseEntity.ok(workspaceService.updateWorkspace(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable UUID id) {
        workspaceService.deleteWorkspace(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<WorkspaceMemberResponse> addMember(Authentication auth, @PathVariable UUID id,
                                                               @Valid @RequestBody AddMemberRequest request) {
        WorkspaceMemberResponse response = workspaceService.addMember(auth.getName(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<WorkspaceMemberResponse>> listMembers(Authentication auth, @PathVariable UUID id) {
        return ResponseEntity.ok(workspaceService.listMembers(auth.getName(), id));
    }

    @PutMapping("/{id}/members/{userId}/role")
    public ResponseEntity<WorkspaceMemberResponse> updateMemberRole(Authentication auth, @PathVariable UUID id,
                                                                      @PathVariable UUID userId,
                                                                      @Valid @RequestBody UpdateMemberRoleRequest request) {
        WorkspaceMemberResponse response = workspaceService.updateMemberRole(auth.getName(), id, userId, request.getRole());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(Authentication auth, @PathVariable UUID id, @PathVariable UUID userId) {
        workspaceService.removeMember(auth.getName(), id, userId);
        return ResponseEntity.noContent().build();
    }
}
