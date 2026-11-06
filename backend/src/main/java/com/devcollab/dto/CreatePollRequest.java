package com.devcollab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreatePollRequest {
    @NotBlank(message = "Question is required")
    @Size(max = 300, message = "Question must be at most 300 characters")
    private String question;

    @NotEmpty(message = "At least 2 options are required")
    @Size(min = 2, max = 10, message = "Polls must have between 2 and 10 options")
    private List<@NotBlank @Size(max = 200) String> options;
}
