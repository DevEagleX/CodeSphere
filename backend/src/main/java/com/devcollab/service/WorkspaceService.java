package com.devcollab.service;

import com.devcollab.dto.*;
import com.devcollab.entity.User;
import com.devcollab.entity.Workspace;
import com.devcollab.entity.WorkspaceMember;
import com.devcollab.exception.DuplicateResourceException;
import com.devcollab.exception.ForbiddenOperationException;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.mapper.WorkspaceMapper;
import com.devcollab.repository.UserRepository;
import com.devcollab.repository.WorkspaceMemberRepository;
import com.devcollab.repository.WorkspaceRepository;
import com.devcollab.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final WorkspaceMapper workspaceMapper;

    @Transactional
    public WorkspaceResponse createWorkspace(String creatorUsername, CreateWorkspaceRequest request) {
        User creator = findUser(creatorUsername);

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(generateUniqueSlug(request.getName()))
                .owner(creator)
                .build();
        workspaceRepository.save(workspace);

        WorkspaceMember ownerMembership = WorkspaceMember.builder()
                .workspace(workspace)
                .user(creator)
                .role(WorkspaceMember.WorkspaceRole.OWNER)
                .build();
        memberRepository.save(ownerMembership);

        return workspaceMapper.toWorkspaceResponse(workspace, 1, WorkspaceMember.WorkspaceRole.OWNER.name());
    }

    public List<WorkspaceResponse> listMyWorkspaces(String username) {
        User user = findUser(username);

        return memberRepository.findByUserId(user.getId()).stream()
                .map(m -> workspaceMapper.toWorkspaceResponse(
                        m.getWorkspace(),
                        memberRepository.countByWorkspaceId(m.getWorkspace().getId()),
                        m.getRole().name()))
                .toList();
    }

    public WorkspaceResponse getWorkspace(String username, UUID workspaceId) {
        Workspace workspace = findWorkspace(workspaceId);
        WorkspaceMember membership = requireMembership(workspace.getId(), username);

        long memberCount = memberRepository.countByWorkspaceId(workspaceId);
        return workspaceMapper.toWorkspaceResponse(workspace, memberCount, membership.getRole().name());
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(String username, UUID workspaceId, UpdateWorkspaceRequest request) {
        Workspace workspace = findWorkspace(workspaceId);
        WorkspaceMember membership = requireMembership(workspaceId, username);
        requireRoleAtLeast(membership, WorkspaceMember.WorkspaceRole.ADMIN);

        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspaceRepository.save(workspace);

        long memberCount = memberRepository.countByWorkspaceId(workspaceId);
        return workspaceMapper.toWorkspaceResponse(workspace, memberCount, membership.getRole().name());
    }

    @Transactional
    public void deleteWorkspace(String username, UUID workspaceId) {
        Workspace workspace = findWorkspace(workspaceId);
        WorkspaceMember membership = requireMembership(workspaceId, username);

        if (membership.getRole() != WorkspaceMember.WorkspaceRole.OWNER) {
            throw new ForbiddenOperationException("Only the workspace owner can delete the workspace");
        }

        workspaceRepository.delete(workspace);
    }

    @Transactional
    public WorkspaceMemberResponse addMember(String requesterUsername, UUID workspaceId, AddMemberRequest request) {
        Workspace workspace = findWorkspace(workspaceId);
        WorkspaceMember requesterMembership = requireMembership(workspaceId, requesterUsername);
        requireRoleAtLeast(requesterMembership, WorkspaceMember.WorkspaceRole.ADMIN);

        User userToAdd = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUsername()));

        if (memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userToAdd.getId())) {
            throw new DuplicateResourceException("User is already a member of this workspace");
        }

        WorkspaceMember newMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(userToAdd)
                .role(request.getRole())
                .build();
        memberRepository.save(newMember);

        return workspaceMapper.toMemberResponse(newMember);
    }

    public List<WorkspaceMemberResponse> listMembers(String username, UUID workspaceId) {
        requireMembership(workspaceId, username);
        return memberRepository.findByWorkspaceId(workspaceId).stream()
                .map(workspaceMapper::toMemberResponse)
                .toList();
    }

    @Transactional
    public WorkspaceMemberResponse updateMemberRole(String requesterUsername, UUID workspaceId, UUID targetUserId,
                                                      WorkspaceMember.WorkspaceRole newRole) {
        WorkspaceMember requesterMembership = requireMembership(workspaceId, requesterUsername);
        requireRoleAtLeast(requesterMembership, WorkspaceMember.WorkspaceRole.ADMIN);

        WorkspaceMember targetMembership = memberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found for user: " + targetUserId));

        // Only an OWNER can grant or revoke OWNER status - an ADMIN promoting someone
        // to OWNER (or demoting the OWNER) would let admins escalate past their own
        // level, which defeats the point of having an OWNER tier at all.
        boolean involvesOwnerRole = newRole == WorkspaceMember.WorkspaceRole.OWNER
                || targetMembership.getRole() == WorkspaceMember.WorkspaceRole.OWNER;
        if (involvesOwnerRole && requesterMembership.getRole() != WorkspaceMember.WorkspaceRole.OWNER) {
            throw new ForbiddenOperationException("Only the workspace owner can change ownership");
        }

        targetMembership.setRole(newRole);
        memberRepository.save(targetMembership);

        return workspaceMapper.toMemberResponse(targetMembership);
    }

    @Transactional
    public void removeMember(String requesterUsername, UUID workspaceId, UUID targetUserId) {
        WorkspaceMember requesterMembership = requireMembership(workspaceId, requesterUsername);
        requireRoleAtLeast(requesterMembership, WorkspaceMember.WorkspaceRole.ADMIN);

        WorkspaceMember targetMembership = memberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found for user: " + targetUserId));

        if (targetMembership.getRole() == WorkspaceMember.WorkspaceRole.OWNER) {
            throw new ForbiddenOperationException("The workspace owner cannot be removed - transfer ownership or delete the workspace instead");
        }

        memberRepository.delete(targetMembership);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    // Package-private so other services (e.g. TagService) can fetch the Workspace
    // entity itself when they need to attach a new child entity to it.
    Workspace findWorkspace(UUID id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + id));
    }

    // Package-private so other services (ChannelService, KnowledgeBaseService, DocumentService)
    // can reuse workspace-membership checks instead of duplicating this logic.
    WorkspaceMember requireMembership(UUID workspaceId, String username) {
        User user = findUser(username);
        return memberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId())
                .orElseThrow(() -> new ForbiddenOperationException("You are not a member of this workspace"));
    }

    void requireRoleAtLeast(WorkspaceMember membership, WorkspaceMember.WorkspaceRole minimumRole) {
        boolean sufficient = membership.getRole().ordinal() <= minimumRole.ordinal();
        if (!sufficient) {
            throw new ForbiddenOperationException("You do not have permission to perform this action");
        }
    }

    private String generateUniqueSlug(String name) {
        String base = SlugUtil.toSlug(name);
        String candidate = base;
        int suffix = 1;
        while (workspaceRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }
}
