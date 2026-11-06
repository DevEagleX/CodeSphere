package com.devcollab.dto;

import com.devcollab.entity.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateChannelRequest {
    @NotBlank(message = "Channel name is required")
    @Size(min = 2, max = 80, message = "Name must be between 2 and 80 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Name may only contain lowercase letters, numbers, and hyphens")
    private String name;

    @Size(max = 300, message = "Description must be at most 300 characters")
    private String description;

    @NotNull(message = "Visibility is required")
    private Channel.Visibility visibility;
}
