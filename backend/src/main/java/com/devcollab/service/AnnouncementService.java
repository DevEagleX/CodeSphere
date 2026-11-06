package com.devcollab.service;

import com.devcollab.dto.AnnouncementResponse;
import com.devcollab.dto.CreateAnnouncementRequest;
import com.devcollab.entity.Announcement;
import com.devcollab.entity.User;
import com.devcollab.entity.WorkspaceMember;
import com.devcollab.exception.ForbiddenOperationException;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.repository.AnnouncementRepository;
import com.devcollab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;

    // Announcements are, by design, restricted to admins/owners to create - unlike
    // Shared Documents (Feature 9), this is a broadcast mechanism, not a
    // collaborative one, so the asymmetry with Document's "anyone can edit" is
    // intentional rather than an inconsistency.
    @Transactional
    public AnnouncementResponse create(String username, UUID workspaceId, CreateAnnouncementRequest request) {
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);
        workspaceService.requireRoleAtLeast(membership, WorkspaceMember.WorkspaceRole.ADMIN);

        User author = findUser(username);

        Announcement announcement = Announcement.builder()
                .workspace(membership.getWorkspace())
                .author(author)
                .title(request.getTitle())
                .body(request.getBody())
                .pinned(request.isPinned())
                .build();
        announcementRepository.save(announcement);

        return toResponse(announcement);
    }

    public List<AnnouncementResponse> list(String username, UUID workspaceId) {
        workspaceService.requireMembership(workspaceId, username);
        return announcementRepository.findByWorkspaceIdOrderByPinnedDescCreatedAtDesc(workspaceId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(String username, UUID workspaceId, UUID announcementId) {
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);
        workspaceService.requireRoleAtLeast(membership, WorkspaceMember.WorkspaceRole.ADMIN);

        Announcement announcement = announcementRepository.findByIdAndWorkspaceId(announcementId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + announcementId));

        announcementRepository.delete(announcement);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private AnnouncementResponse toResponse(Announcement a) {
        return AnnouncementResponse.builder()
                .id(a.getId())
                .workspaceId(a.getWorkspace().getId())
                .title(a.getTitle())
                .body(a.getBody())
                .pinned(a.isPinned())
                .authorId(a.getAuthor().getId())
                .authorUsername(a.getAuthor().getUsername())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
