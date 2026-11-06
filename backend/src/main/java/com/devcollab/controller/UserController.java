package com.devcollab.controller;

import com.devcollab.dto.ProfileResponse;
import com.devcollab.dto.UpdateProfileRequest;
import com.devcollab.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentUser(Authentication authentication) {
        ProfileResponse response = userService.getProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ProfileResponse> updateProfile(Authentication authentication,
                                                           @Valid @RequestBody UpdateProfileRequest request) {
        ProfileResponse response = userService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }
}
