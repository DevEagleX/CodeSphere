package com.devcollab.mapper;

import com.devcollab.dto.WorkspaceMemberResponse;
import com.devcollab.dto.WorkspaceResponse;
import com.devcollab.entity.Workspace;
import com.devcollab.entity.WorkspaceMember;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMapper {

    public WorkspaceResponse toWorkspaceResponse(Workspace workspace, long memberCount, String currentUserRole) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .slug(workspace.getSlug())
                .description(workspace.getDescription())
                .ownerId(workspace.getOwner().getId())
                .ownerUsername(workspace.getOwner().getUsername())
                .memberCount(memberCount)
                .currentUserRole(currentUserRole)
                .createdAt(workspace.getCreatedAt())
                .build();
    }

    public WorkspaceMemberResponse toMemberResponse(WorkspaceMember member) {
        return WorkspaceMemberResponse.builder()
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .role(member.getRole().name())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
