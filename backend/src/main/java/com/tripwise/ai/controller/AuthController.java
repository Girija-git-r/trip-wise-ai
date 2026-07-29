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
 * Registration/login themselves happen entirely against Supabase Auth on the
 * frontend (supabase-js) — this backend never sees a password. This endpoint
 * just confirms a Supabase token is valid and returns the synced profile.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUserResponse(authentication));
    }
}
