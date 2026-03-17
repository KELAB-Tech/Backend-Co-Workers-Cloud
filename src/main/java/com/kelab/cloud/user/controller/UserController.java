package com.kelab.cloud.user.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.kelab.cloud.cloudinary.service.CloudinaryService;
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
    private final CloudinaryService cloudinaryService;

    // =========================================================
    // GET MY PROFILE
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

    // =========================================================
    // UPLOAD AVATAR
    // POST /api/users/upload/avatar
    // =========================================================
    @PostMapping("/upload/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        // ✅ Nombre correcto del método en CloudinaryService
        String url = cloudinaryService.uploadImage(file, "avatars");

        return ResponseEntity.ok(Map.of("url", url));
    }
}