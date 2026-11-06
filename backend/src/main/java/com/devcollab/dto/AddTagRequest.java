package com.devcollab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddTagRequest {
    @NotBlank(message = "Tag name is required")
    @Size(min = 1, max = 50, message = "Tag name must be at most 50 characters")
    private String name;
}
