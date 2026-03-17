package com.kelab.cloud.category.controller;

import com.kelab.cloud.category.dto.CategoryRequest;
import com.kelab.cloud.category.dto.CategoryResponse;
import com.kelab.cloud.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // =========================================================
    // PUBLIC — listar categorías activas
    // GET /api/categories
    // =========================================================
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllActiveCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    // =========================================================
    // ADMIN — listar todas
    // GET /api/categories/admin
    // =========================================================
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // =========================================================
    // ADMIN — crear
    // POST /api/categories
    // =========================================================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(201).body(categoryService.createCategory(request));
    }

    // =========================================================
    // ADMIN — editar
    // PUT /api/categories/{id}
    // =========================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    // =========================================================
    // ADMIN — toggle active/inactive
    // PATCH /api/categories/{id}/toggle
    // =========================================================
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleCategoryStatus(@PathVariable Long id) {
        categoryService.toggleCategoryStatus(id);
        return ResponseEntity.noContent().build();
    }
}