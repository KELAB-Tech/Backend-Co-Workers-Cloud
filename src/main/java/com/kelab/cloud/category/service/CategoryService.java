package com.kelab.cloud.category.service;

import com.kelab.cloud.category.dto.CategoryRequest;
import com.kelab.cloud.category.dto.CategoryResponse;
import com.kelab.cloud.category.model.Category;
import com.kelab.cloud.category.repo.CategoryRepository;
import com.kelab.cloud.marketplace.repo.ProductRepository;
import com.kelab.cloud.marketplace.model.ProductStatus;

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
    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // ADMIN — listar todas (incluyendo inactivas)
    // =========================================================
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

        // Validar que el nuevo nombre no exista en otra categoría
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
    // PRIVATE
    // =========================================================
    private CategoryResponse mapToResponse(Category category) {

        long productCount = (category.getProducts() == null)
                ? 0
                : category.getProducts().stream()
                        .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                        .count();

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