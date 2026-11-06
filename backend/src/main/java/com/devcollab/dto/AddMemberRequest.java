package com.devcollab.dto;

import com.devcollab.entity.WorkspaceMember;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddMemberRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "Role is required")
    private WorkspaceMember.WorkspaceRole role;
}
