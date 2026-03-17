package com.kelab.cloud.cloudinary.controller;

import com.kelab.cloud.cloudinary.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class ImageUploadController {

    private final CloudinaryService cloudinaryService;

    // =====================================================
    // PRODUCTO — sube imagen principal o adicional
    // POST /api/upload/product
    // =====================================================
    @PostMapping("/product")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> uploadProductImage(
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        String url = cloudinaryService.uploadImage(file, "products");

        return ResponseEntity.ok(Map.of(
                "url", url,
                "message", "Imagen de producto subida correctamente"));
    }

    // =====================================================
    // TIENDA — sube logo o imagen de la tienda
    // POST /api/upload/store
    // =====================================================
    @PostMapping("/store")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> uploadStoreImage(
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        String url = cloudinaryService.uploadImage(file, "stores");

        return ResponseEntity.ok(Map.of(
                "url", url,
                "message", "Imagen de tienda subida correctamente"));
    }

    // =====================================================
    // USUARIO — sube foto de perfil
    // POST /api/upload/avatar
    // =====================================================
    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        String url = cloudinaryService.uploadImage(file, "avatars");

        return ResponseEntity.ok(Map.of(
                "url", url,
                "message", "Avatar subido correctamente"));
    }
}