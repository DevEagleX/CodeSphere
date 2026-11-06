package com.devcollab.service;

import com.devcollab.entity.User;
import com.devcollab.entity.Workspace;
import com.devcollab.entity.WorkspaceMember;
import com.devcollab.exception.ForbiddenOperationException;
import com.devcollab.mapper.WorkspaceMapper;
import com.devcollab.repository.UserRepository;
import com.devcollab.repository.WorkspaceMemberRepository;
import com.devcollab.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Focused on the authorization logic that every other feature's service reuses
 * (requireMembership / requireRoleAtLeast) - a regression here would silently
 * weaken access control across Channels, Discussions, KB, Documents, etc.
 */
@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private WorkspaceMemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkspaceMapper workspaceMapper;

    @InjectMocks
    private WorkspaceService workspaceService;

    private User memberUser;
    private UUID workspaceId;

    @BeforeEach
    void setUp() {
        memberUser = User.builder().id(UUID.randomUUID()).username("bob").build();
        workspaceId = UUID.randomUUID();
    }

    @Test
    void deleteWorkspace_throwsForbidden_whenCallerIsNotOwner() {
        Workspace workspace = Workspace.builder().id(workspaceId).owner(memberUser).build();
        WorkspaceMember membership = WorkspaceMember.builder()
                .workspace(workspace).user(memberUser).role(WorkspaceMember.WorkspaceRole.ADMIN).build();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(memberUser));
        when(memberRepository.findByWorkspaceIdAndUserId(workspaceId, memberUser.getId()))
                .thenReturn(Optional.of(membership));

        // ADMIN (not OWNER) must not be able to delete the workspace - this is the
        // one operation restricted more tightly than the usual ADMIN-or-above bar.
        assertThatThrownBy(() -> workspaceService.deleteWorkspace("bob", workspaceId))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireMembership_throwsForbidden_whenUserIsNotAMember() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(memberUser));
        when(memberRepository.findByWorkspaceIdAndUserId(workspaceId, memberUser.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.requireMembership(workspaceId, "bob"))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireRoleAtLeast_allowsOwner_forAdminRequirement() {
        WorkspaceMember ownerMembership = WorkspaceMember.builder()
                .user(memberUser).role(WorkspaceMember.WorkspaceRole.OWNER).build();

        // OWNER satisfies an ADMIN-or-above requirement - should not throw.
        workspaceService.requireRoleAtLeast(ownerMembership, WorkspaceMember.WorkspaceRole.ADMIN);
    }

    @Test
    void requireRoleAtLeast_rejectsMember_forAdminRequirement() {
        WorkspaceMember memberMembership = WorkspaceMember.builder()
                .user(memberUser).role(WorkspaceMember.WorkspaceRole.MEMBER).build();

        assertThatThrownBy(() -> workspaceService.requireRoleAtLeast(memberMembership, WorkspaceMember.WorkspaceRole.ADMIN))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
