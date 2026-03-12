package com.kelab.cloud.user.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.kelab.cloud.user.dto.UserProfileResponse;
import com.kelab.cloud.user.dto.UserSelfUpdateRequest;
import com.kelab.cloud.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // =========================================================
    // GET MY PROFILE — lee email del JWT, sin exponer ID en URL
    // GET /api/users/me
    // =========================================================
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getMyProfile(Principal principal) {

        return ResponseEntity.ok(
                userService.getMyProfile(principal.getName()));
    }

    // =========================================================
    // UPDATE MY PROFILE — solo nombre y email
    // PUT /api/users/me
    // =========================================================
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UserSelfUpdateRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                userService.updateMyProfile(principal.getName(), request));
    }
}