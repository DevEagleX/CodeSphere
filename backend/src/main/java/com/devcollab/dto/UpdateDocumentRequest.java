package com.devcollab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDocumentRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 50_000, message = "Body must be at most 50,000 characters")
    private String body;
}
