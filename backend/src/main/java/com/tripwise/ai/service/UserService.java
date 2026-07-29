package com.tripwise.ai.service;

import com.tripwise.ai.dto.auth.UserResponse;
import com.tripwise.ai.entity.User;
import com.tripwise.ai.exception.ResourceNotFoundException;
import com.tripwise.ai.repository.UserRepository;
import com.tripwise.ai.security.SupabaseUserClaims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getCurrentUser(Authentication authentication) {
        SupabaseUserClaims claims = (SupabaseUserClaims) authentication.getPrincipal();
        return userRepository.findById(claims.id())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserResponse getCurrentUserResponse(Authentication authentication) {
        return toResponse(getCurrentUser(authentication));
    }

    /**
     * Keeps the local profile row in step with Supabase Auth. Supabase is the
     * source of truth for identity — name/email edits happen there (via
     * supabase-js on the frontend); this just mirrors the latest claims.
     */
    @Transactional
    public void syncFromToken(SupabaseUserClaims claims) {
        User user = userRepository.findById(claims.id()).orElse(null);

        if (user == null) {
            userRepository.save(User.builder()
                    .id(claims.id())
                    .name(claims.name())
                    .email(claims.email())
                    .build());
            return;
        }

        boolean changed = false;
        if (!user.getName().equals(claims.name())) {
            user.setName(claims.name());
            changed = true;
        }
        if (!user.getEmail().equals(claims.email())) {
            user.setEmail(claims.email());
            changed = true;
        }
        if (changed) {
            userRepository.save(user);
        }
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId().toString())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
