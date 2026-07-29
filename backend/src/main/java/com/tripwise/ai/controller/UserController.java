package com.tripwise.ai.controller;

import com.tripwise.ai.dto.auth.UpdateProfileRequest;
import com.tripwise.ai.dto.auth.UserResponse;
import com.tripwise.ai.service.UserService;
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

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUserResponse(authentication));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(Authentication authentication,
                                                        @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(authentication, request));
    }
}
