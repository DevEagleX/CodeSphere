package com.devcollab.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddChannelMemberRequest {
    @NotBlank(message = "Username is required")
    private String username;
}
