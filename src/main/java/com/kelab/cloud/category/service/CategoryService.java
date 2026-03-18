package com.kelab.cloud.category.service;

import com.kelab.cloud.category.dto.CategoryRequest;
import com.kelab.cloud.category.dto.CategoryResponse;
import com.kelab.cloud.category.model.Category;
import com.kelab.cloud.category.repo.CategoryRepository;
import com.kelab.cloud.marketplace.model.ProductStatus;
import com.kelab.cloud.marketplace.repo.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // =========================================================
    // PUBLIC — listar categorías activas con conteo de productos
    // =========================================================
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // ADMIN — listar todas (incluyendo inactivas)
    // =========================================================
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // ADMIN — crear categoría
    // =========================================================
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new IllegalStateException("Ya existe una categoría con ese nombre");
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .icon(request.getIcon())
                .active(true)
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    // =========================================================
    // ADMIN — editar categoría
    // =========================================================
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        categoryRepository.findByNameIgnoreCase(request.getName().trim())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalStateException("Ya existe una categoría con ese nombre");
                    }
                });

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());

        return mapToResponse(categoryRepository.save(category));
    }

    // =========================================================
    // ADMIN — activar / desactivar
    // =========================================================
    @Transactional
    public void toggleCategoryStatus(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        category.setActive(!category.isActive());
        categoryRepository.save(category);
    }

    // =========================================================
    // PRIVATE — sin acceder a colecciones lazy
    // =========================================================
    private CategoryResponse mapToResponse(Category category) {

        // ✅ Contamos con query directa — NUNCA tocamos category.getProducts()
        long productCount = productRepository
                .countByCategoryAndStatus(category, ProductStatus.ACTIVE);

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .icon(category.getIcon())
                .active(category.isActive())
                .productCount(productCount)
                .build();
    }
}