package com.devcollab.controller;

import com.devcollab.dto.AdminUserResponse;
import com.devcollab.dto.AdminWorkspaceResponse;
import com.devcollab.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> listUsers(Authentication auth) {
        return ResponseEntity.ok(adminService.listAllUsers(auth.getName()));
    }

    @PutMapping("/users/{id}/enable")
    public ResponseEntity<AdminUserResponse> enableUser(Authentication auth, @PathVariable UUID id) {
        return ResponseEntity.ok(adminService.setUserEnabled(auth.getName(), id, true));
    }

    @PutMapping("/users/{id}/disable")
    public ResponseEntity<AdminUserResponse> disableUser(Authentication auth, @PathVariable UUID id) {
        return ResponseEntity.ok(adminService.setUserEnabled(auth.getName(), id, false));
    }

    @GetMapping("/workspaces")
    public ResponseEntity<List<AdminWorkspaceResponse>> listWorkspaces(Authentication auth) {
        return ResponseEntity.ok(adminService.listAllWorkspaces(auth.getName()));
    }
}
