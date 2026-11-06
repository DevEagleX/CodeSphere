package com.devcollab.controller;

import com.devcollab.dto.WorkspaceAnalyticsResponse;
import com.devcollab.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/api/workspaces/{workspaceId}/analytics")
    public ResponseEntity<WorkspaceAnalyticsResponse> getAnalytics(Authentication auth, @PathVariable UUID workspaceId) {
        return ResponseEntity.ok(analyticsService.getWorkspaceAnalytics(auth.getName(), workspaceId));
    }
}
