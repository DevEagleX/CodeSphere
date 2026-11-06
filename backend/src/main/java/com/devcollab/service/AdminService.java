package com.devcollab.service;

import com.devcollab.dto.AdminUserResponse;
import com.devcollab.dto.AdminWorkspaceResponse;
import com.devcollab.entity.User;
import com.devcollab.entity.Workspace;
import com.devcollab.exception.ForbiddenOperationException;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.repository.UserRepository;
import com.devcollab.repository.WorkspaceMemberRepository;
import com.devcollab.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Platform-level administration, gated by User.Role.ADMIN (Feature 1's global
 * role) - deliberately separate from WorkspaceMember.WorkspaceRole (Feature 3),
 * which only grants authority within a single workspace. A workspace OWNER has
 * no special access here; a platform ADMIN has no special access inside a
 * workspace they haven't joined. Keeping these two role systems orthogonal avoids
 * the confusion of a single "admin" concept meaning two different things.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public List<AdminUserResponse> listAllUsers(String requesterUsername) {
        requirePlatformAdmin(requesterUsername);
        return userRepository.findAll().stream().map(this::toUserResponse).toList();
    }

    @Transactional
    public AdminUserResponse setUserEnabled(String requesterUsername, UUID targetUserId, boolean enabled) {
        requirePlatformAdmin(requesterUsername);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + targetUserId));

        target.setEnabled(enabled);
        userRepository.save(target);

        return toUserResponse(target);
    }

    public List<AdminWorkspaceResponse> listAllWorkspaces(String requesterUsername) {
        requirePlatformAdmin(requesterUsername);

        return workspaceRepository.findAll().stream()
                .map(w -> AdminWorkspaceResponse.builder()
                        .id(w.getId())
                        .name(w.getName())
                        .slug(w.getSlug())
                        .ownerUsername(w.getOwner().getUsername())
                        .memberCount(workspaceMemberRepository.countByWorkspaceId(w.getId()))
                        .createdAt(w.getCreatedAt())
                        .build())
                .toList();
    }

    private void requirePlatformAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (user.getRole() != User.Role.ADMIN) {
            throw new ForbiddenOperationException("This action requires platform administrator privileges");
        }
    }

    private AdminUserResponse toUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
