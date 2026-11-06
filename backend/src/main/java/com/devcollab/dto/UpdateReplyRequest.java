package com.devcollab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReplyRequest {
    @NotBlank(message = "Body is required")
    @Size(max = 50_000, message = "Body must be at most 50,000 characters")
    private String body;
}
