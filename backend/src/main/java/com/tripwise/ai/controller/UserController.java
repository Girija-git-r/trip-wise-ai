package com.tripwise.ai.controller;

import com.tripwise.ai.dto.auth.UserResponse;
import com.tripwise.ai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Name/email edits go straight to Supabase (via supabase-js on the frontend)
 * since Supabase Auth is the source of truth for identity; this controller
 * is read-only and just reflects the synced local profile.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUserResponse(authentication));
    }
}
