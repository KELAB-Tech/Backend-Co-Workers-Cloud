package com.kelab.cloud.store.controller;

import com.kelab.cloud.store.dto.StoreRequest;
import com.kelab.cloud.store.dto.StoreResponse;
import com.kelab.cloud.marketplace.dto.ProductResponse;
import com.kelab.cloud.marketplace.service.ProductService;
import com.kelab.cloud.store.dto.StoreImageResponse;
import com.kelab.cloud.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final ProductService productService;

    // ==============================
    // ADMIN - Approve Store
    // ==============================
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreResponse> approveStore(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.approveStore(id));
    }

    // ==============================
    // PUBLIC - List Stores (only approved)
    // ==============================
    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAllStores(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name) {

        return ResponseEntity.ok(storeService.getAllStores(city, name));
    }

    // ==============================
    // PUBLIC - Get Store by ID
    // ==============================
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    // =========================================================
    // PUBLIC ENDPOINTS
    // =========================================================

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ProductResponse>> getProductsByStore(
            @PathVariable Long storeId) {

        return ResponseEntity.ok(
                productService.getProductsByStore(storeId));
    }

    // ==============================
    // COMPANY - Create Store
    // ==============================
    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
            @Valid @RequestBody StoreRequest request,
            Principal principal) {

        StoreResponse response = storeService.createStore(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==============================
    // OWNER - Update Store
    // ==============================
    @PutMapping("/{id}")
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody StoreRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                storeService.updateStore(id, request, principal.getName()));
    }

    // ==============================
    // OWNER - Delete Store (Soft Delete)
    // ==============================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id, Principal principal) {
        storeService.deleteStore(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // ==============================
    // OWNER - Get My Store
    // ==============================
    @GetMapping("/me")
    public ResponseEntity<StoreResponse> getMyStore(Principal principal) {
        return ResponseEntity.ok(
                storeService.getMyStore(principal.getName()));
    }

    // ==============================
    // OWNER - Add Image
    // ==============================
    @PostMapping("/{storeId}/images")
    public ResponseEntity<StoreImageResponse> addImage(
            @PathVariable Long storeId,
            @RequestParam String imageUrl,
            @RequestParam(required = false) String description,
            Principal principal) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storeService.addStoreImage(storeId, imageUrl, description, principal.getName()));
    }

    // ==============================
    // PUBLIC - Get Store Images
    // ==============================
    @GetMapping("/{storeId}/images")
    public ResponseEntity<List<StoreImageResponse>> getImages(@PathVariable Long storeId) {
        return ResponseEntity.ok(storeService.getStoreImages(storeId));
    }

    // ==============================
    // OWNER - Delete Image (Soft Delete)
    // ==============================
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId, Principal principal) {
        storeService.deleteStoreImage(imageId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}