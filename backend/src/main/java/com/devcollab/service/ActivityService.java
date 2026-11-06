package com.devcollab.service;

import com.devcollab.dto.ActivityEventResponse;
import com.devcollab.dto.PageResponse;
import com.devcollab.entity.ActivityEvent;
import com.devcollab.entity.User;
import com.devcollab.entity.Workspace;
import com.devcollab.repository.ActivityEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityEventRepository activityEventRepository;
    private final WorkspaceService workspaceService;

    // Called internally by other services right after a create-type action succeeds.
    // No authorization check here - by the time this is called, the caller has
    // already passed whatever authorization the triggering action required.
    @Transactional
    public void log(Workspace workspace, User actor, ActivityEvent.ActivityType type, String description) {
        ActivityEvent event = ActivityEvent.builder()
                .workspace(workspace)
                .actor(actor)
                .type(type)
                .description(description)
                .build();
        activityEventRepository.save(event);
    }

    public PageResponse<ActivityEventResponse> listFeed(String username, UUID workspaceId, Pageable pageable) {
        workspaceService.requireMembership(workspaceId, username);

        Page<ActivityEvent> page = activityEventRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    private ActivityEventResponse toResponse(ActivityEvent e) {
        return ActivityEventResponse.builder()
                .id(e.getId())
                .type(e.getType().name())
                .description(e.getDescription())
                .actorId(e.getActor().getId())
                .actorUsername(e.getActor().getUsername())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
