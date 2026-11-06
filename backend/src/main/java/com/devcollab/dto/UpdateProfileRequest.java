package com.devcollab.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class UpdateProfileRequest {
    @Size(max = 100, message = "Display name must be at most 100 characters")
    private String displayName;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;

    @URL(message = "Avatar URL must be a valid URL")
    @Size(max = 500, message = "Avatar URL must be at most 500 characters")
    private String avatarUrl;
}
