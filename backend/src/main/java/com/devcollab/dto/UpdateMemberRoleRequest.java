package com.devcollab.dto;

import com.devcollab.entity.WorkspaceMember;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRoleRequest {
    @NotNull(message = "Role is required")
    private WorkspaceMember.WorkspaceRole role;
}
