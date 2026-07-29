package com.tripwise.ai.service;

import com.tripwise.ai.dto.auth.UpdateProfileRequest;
import com.tripwise.ai.dto.auth.UserResponse;
import com.tripwise.ai.entity.User;
import com.tripwise.ai.exception.DuplicateEmailException;
import com.tripwise.ai.exception.ResourceNotFoundException;
import com.tripwise.ai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserResponse getCurrentUserResponse(Authentication authentication) {
        return toResponse(getCurrentUser(authentication));
    }

    @Transactional
    public UserResponse updateProfile(Authentication authentication, UpdateProfileRequest request) {
        User user = getCurrentUser(authentication);

        String newEmail = request.email().toLowerCase().trim();
        if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new DuplicateEmailException("An account with this email already exists");
        }

        user.setName(request.name());
        user.setEmail(newEmail);
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
