package com.devcollab.controller;

import com.devcollab.dto.ActivityEventResponse;
import com.devcollab.dto.PageResponse;
import com.devcollab.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/api/workspaces/{workspaceId}/activity")
    public ResponseEntity<PageResponse<ActivityEventResponse>> feed(Authentication auth,
                                                                      @PathVariable UUID workspaceId,
                                                                      @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(activityService.listFeed(auth.getName(), workspaceId, pageable));
    }
}
