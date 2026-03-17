package com.kelab.cloud.admin.controller;

import com.kelab.cloud.admin.dto.AdminDashboardResponse;
import com.kelab.cloud.admin.service.AdminService;
import com.kelab.cloud.common.dto.PagedResponse;
import com.kelab.cloud.common.util.SortValidator;
import com.kelab.cloud.marketplace.dto.ProductResponse;
import com.kelab.cloud.marketplace.service.ProductService;
import com.kelab.cloud.store.dto.StoreResponse;
import com.kelab.cloud.store.model.StoreStatus;
import com.kelab.cloud.store.service.StoreService;
import com.kelab.cloud.user.dto.UserProfileResponse;
import com.kelab.cloud.user.dto.UserUpdateRequest;
import com.kelab.cloud.user.model.ActorType;
import com.kelab.cloud.user.model.TipoPersona;
import com.kelab.cloud.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final StoreService storeService;
    private final ProductService productService;
    private final UserService userService;

    // Campos permitidos para ordenar — evita inyección por sortBy
    private static final List<String> USER_SORT_FIELDS = List.of("name", "email", "createdAt", "actorType",
            "tipoPersona");

    private static final List<String> STORE_SORT_FIELDS = List.of("name", "city", "createdAt", "status");

    private static final List<String> PRODUCT_SORT_FIELDS = List.of("name", "price", "createdAt", "stock");

    // ==============================
    // DASHBOARD
    // ==============================
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    // ==============================
    // STORES
    // ==============================

    @GetMapping("/stores")
    public ResponseEntity<PagedResponse<StoreResponse>> getAllStores(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) StoreStatus status,
            @RequestParam(required = false) ActorType actorType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = SortValidator.validate(sortBy, STORE_SORT_FIELDS, "createdAt");

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                storeService.getAllStoresAdmin(city, name, status, actorType, pageable));
    }

    @GetMapping("/stores/pending")
    public ResponseEntity<PagedResponse<StoreResponse>> getPendingStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(
                storeService.getAllStoresAdmin(null, null, StoreStatus.PENDING, null, pageable));
    }

    @PutMapping("/stores/{id}/approve")
    public ResponseEntity<StoreResponse> approveStore(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveStore(id));
    }

    @PutMapping("/stores/{id}/suspend")
    public ResponseEntity<StoreResponse> suspendStore(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.suspendStore(id));
    }

    // ==============================
    // PRODUCTS
    // ==============================

    @GetMapping("/products")
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = SortValidator.validate(sortBy, PRODUCT_SORT_FIELDS, "createdAt");

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    // ==============================
    // USERS
    // ==============================

    @GetMapping("/users")
    public ResponseEntity<PagedResponse<UserProfileResponse>> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) TipoPersona tipoPersona,
            @RequestParam(required = false) ActorType actorType,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = SortValidator.validate(sortBy, USER_SORT_FIELDS, "createdAt");

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                userService.getAllUsers(name, email, tipoPersona, actorType, enabled, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<Void> suspendUser(@PathVariable Long id) {
        userService.suspendUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserProfileResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}