package com.kelab.cloud.admin.controller;

import com.kelab.cloud.admin.dto.AdminDashboardResponse;
import com.kelab.cloud.admin.service.AdminService;
import com.kelab.cloud.marketplace.dto.ProductResponse;
import com.kelab.cloud.marketplace.service.ProductService;
import com.kelab.cloud.store.dto.StoreResponse;
import com.kelab.cloud.store.model.StoreStatus;
import com.kelab.cloud.store.service.StoreService;
import com.kelab.cloud.user.dto.UserProfileResponse;
import com.kelab.cloud.user.dto.UserUpdateRequest;
import com.kelab.cloud.user.model.Role;
import com.kelab.cloud.user.model.User;
import com.kelab.cloud.user.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final StoreService storeService;
    private final ProductService productService;
    private final UserService userService;

    // ==============================
    // DASHBOARD
    // ==============================
    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboard() {
        return adminService.getDashboard();
    }

    // ==============================
    // STORES
    // ==============================

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<StoreResponse> getPendingStores() {
        return storeService.getStoresByStatus(StoreStatus.PENDING);
    }

    @PutMapping("/stores/{id}/approve")
    public StoreResponse approveStore(@PathVariable Long id) {
        return adminService.approveStore(id);
    }

    @PutMapping("/{id}/suspend")
    public StoreResponse suspendStore(@PathVariable Long id) {
        return storeService.suspendStore(id);
    }

    // =========================================================
    // Listar todos los productos (ADMIN)
    // =========================================================
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProductsForAdmin() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // ==============================
    // USERS
    // ==============================
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PatchMapping("/{id}/activate")
    public void activateUser(@PathVariable Long id) {
        userService.activateUser(id);
    }

    @PatchMapping("/{id}/suspend")
    public void suspendUser(@PathVariable Long id) {
        userService.suspendUser(id);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserProfileResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {

        User updatedUser = userService.updateUser(id, request);

        UserProfileResponse response = UserProfileResponse.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .tipoPersona(updatedUser.getTipoPersona())
                .actorType(updatedUser.getActorType())
                .roles(updatedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();

        return ResponseEntity.ok(response);
    }

}