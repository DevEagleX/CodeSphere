package com.devcollab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateArticleRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Body is required")
    @Size(max = 50_000, message = "Body must be at most 50,000 characters")
    private String body;

    private UUID categoryId;

    @NotNull(message = "Published flag is required")
    private Boolean published;
}
