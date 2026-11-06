package com.devcollab.service;

import com.devcollab.dto.ProfileResponse;
import com.devcollab.dto.UpdateProfileRequest;
import com.devcollab.entity.User;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.mapper.UserMapper;
import com.devcollab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public ProfileResponse getProfile(String username) {
        User user = findByUsername(username);
        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findByUsername(username);

        user.setDisplayName(request.getDisplayName());
        user.setBio(request.getBio());
        user.setAvatarUrl(request.getAvatarUrl());

        userRepository.save(user);
        return userMapper.toProfileResponse(user);
    }

    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
