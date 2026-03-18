package com.kelab.cloud.marketplace.service;

import com.kelab.cloud.marketplace.dto.ProductRequest;
import com.kelab.cloud.marketplace.dto.ProductResponse;
import com.kelab.cloud.marketplace.model.*;
import com.kelab.cloud.category.model.Category;
import com.kelab.cloud.category.repo.CategoryRepository;
import com.kelab.cloud.common.dto.PagedResponse;
import com.kelab.cloud.marketplace.repo.ProductImageRepository;
import com.kelab.cloud.marketplace.repo.ProductRepository;
import com.kelab.cloud.store.model.Store;
import com.kelab.cloud.store.model.StoreStatus;
import com.kelab.cloud.store.repo.StoreRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;

    // =========================================================
    // ADMIN - List All Products
    // =========================================================
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAllProducts(Pageable pageable) {
        return PagedResponse.of(
                productRepository.findAll(pageable).map(this::mapToResponse));
    }

    // =========================================================
    // CREATE PRODUCT (OWNER)
    // =========================================================
    @Transactional
    public ProductResponse createProduct(Long storeId, ProductRequest request, String email) {

        Store store = findStoreOrThrow(storeId);
        validateOwnership(store, email);
        validateStoreApproved(store);

        String trimmedName = request.getName().trim();

        if (productRepository.existsByStoreAndNameIgnoreCase(store, trimmedName)) {
            throw new IllegalStateException("Ya existe un producto con ese nombre en esta store");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .filter(Category::isActive)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada o inactiva"));
        }

        Product product = Product.builder()
                .name(trimmedName)
                .description(request.getDescription().trim())
                .price(request.getPrice())
                .stock(request.getStock())
                .mainImageUrl(request.getMainImageUrl().trim())
                .category(category)
                .store(store)
                .createdBy(email)
                .build();

        return mapToResponse(productRepository.save(product));
    }

    // =========================================================
    // MARKETPLACE GLOBAL
    // =========================================================
    @Transactional(readOnly = true)
    public Page<ProductResponse> getMarketplaceProducts(
            String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            String city, Integer page, Integer size, String sortBy, String sortDir) {

        if (name == null)
            name = "";
        if (minPrice == null)
            minPrice = BigDecimal.ZERO;
        if (maxPrice == null)
            maxPrice = BigDecimal.valueOf(Long.MAX_VALUE);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(direction, sortBy != null ? sortBy : "createdAt"));

        Page<Product> result;

        if (categoryId != null && city != null && !city.isBlank()) {
            Category cat = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
            result = productRepository
                    .findByStatusAndCategoryAndStore_CityIgnoreCaseAndNameContainingIgnoreCaseAndPriceBetween(
                            ProductStatus.ACTIVE, cat, city, name, minPrice, maxPrice, pageable);
        } else if (categoryId != null) {
            Category cat = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
            result = productRepository
                    .findByStatusAndCategoryAndNameContainingIgnoreCaseAndPriceBetween(
                            ProductStatus.ACTIVE, cat, name, minPrice, maxPrice, pageable);
        } else if (city != null && !city.isBlank()) {
            result = productRepository
                    .findByStatusAndStore_CityIgnoreCaseAndNameContainingIgnoreCaseAndPriceBetween(
                            ProductStatus.ACTIVE, city, name, minPrice, maxPrice, pageable);
        } else {
            result = productRepository
                    .findByStatusAndNameContainingIgnoreCaseAndPriceBetween(
                            ProductStatus.ACTIVE, name, minPrice, maxPrice, pageable);
        }

        return result.map(this::mapToResponse);
    }

    // =========================================================
    // PÚBLICO
    // =========================================================
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        return productRepository.findByCategoryAndStatus(category, ProductStatus.ACTIVE)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByStore(Long storeId) {
        Store store = findStoreOrThrow(storeId);
        validateStorePublicAccess(store);
        return productRepository.findByStoreAndStatus(store, ProductStatus.ACTIVE)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        Product product = findProductOrThrow(productId);
        validateStorePublicAccess(product.getStore());
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts(Long storeId) {
        Store store = findStoreOrThrow(storeId);
        validateStorePublicAccess(store);
        return productRepository.findByStoreAndFeaturedTrueAndStatus(store, ProductStatus.ACTIVE)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getGlobalFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndStatus(ProductStatus.ACTIVE)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getOutOfStockProducts(Long storeId) {
        Store store = findStoreOrThrow(storeId);
        validateStorePublicAccess(store);
        return productRepository.findByStoreAndStatus(store, ProductStatus.OUT_OF_STOCK)
                .stream().map(this::mapToResponse).toList();
    }

    // =========================================================
    // OWNER VIEW
    // =========================================================
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProductsByStoreForOwner(Long storeId, String email) {
        Store store = findStoreOrThrow(storeId);
        validateOwnership(store, email);
        return productRepository.findByStore(store)
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getMyStoreProducts(String email) {
        Store store = storeRepository.findByOwnerEmail(email)
                .orElseThrow(() -> new IllegalStateException("No tienes ninguna tienda registrada"));
        return productRepository.findByStore(store)
                .stream().map(this::mapToResponse).toList();
    }

    // =========================================================
    // UPDATE (OWNER)
    // =========================================================
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest request, String email) {

        Product product = findProductOrThrow(productId);
        validateOwnership(product.getStore(), email);

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .filter(Category::isActive)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada o inactiva"));
        }

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setMainImageUrl(request.getMainImageUrl().trim());
        product.setCategory(category);
        product.setUpdatedBy(email);

        return mapToResponse(productRepository.save(product));
    }

    // =========================================================
    // SOFT DELETE (OWNER)
    // =========================================================
    @Transactional
    public void deleteProduct(Long productId, String email) {
        Product product = findProductOrThrow(productId);
        validateOwnership(product.getStore(), email);
        product.deactivate();
    }

    // =========================================================
    // ADMIN MODERATION
    // =========================================================
    @Transactional
    public void adminDeactivateProduct(Long productId) {
        findProductOrThrow(productId).deactivate();
    }

    @Transactional
    public void adminActivateProduct(Long productId) {
        Product product = findProductOrThrow(productId);
        product.setStatus(product.getStock() == 0
                ? ProductStatus.OUT_OF_STOCK
                : ProductStatus.ACTIVE);
    }

    @Transactional
    public void adminToggleFeatured(Long productId) {
        Product product = findProductOrThrow(productId);
        product.setFeatured(!product.isFeatured());
    }

    // =========================================================
    // IMAGES
    // =========================================================
    @Transactional
    public void addProductImage(Long productId, String imageUrl, String email) {
        Product product = findProductOrThrow(productId);
        validateOwnership(product.getStore(), email);
        productImageRepository.save(
                ProductImage.builder().product(product).imageUrl(imageUrl.trim()).build());
    }

    @Transactional(readOnly = true)
    public List<String> getProductImages(Long productId) {
        Product product = findProductOrThrow(productId);
        return productImageRepository.findByProductAndStatus(product, ImageStatus.ACTIVE)
                .stream().map(ProductImage::getImageUrl).toList();
    }

    @Transactional
    public void deleteProductImage(Long imageId, String email) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada"));
        validateOwnership(image.getProduct().getStore(), email);
        image.deactivate();
    }

    // =========================================================
    // PAGINACIÓN + FILTROS (por STORE)
    // =========================================================
    @Transactional(readOnly = true)
    public Page<ProductResponse> filterProductsPaged(
            Long storeId, String name, BigDecimal minPrice, BigDecimal maxPrice,
            List<ProductStatus> statusList, Integer page, Integer size,
            String sortBy, String sortDir) {

        Store store = findStoreOrThrow(storeId);
        validateStorePublicAccess(store);

        if (name == null)
            name = "";
        if (minPrice == null)
            minPrice = BigDecimal.ZERO;
        if (maxPrice == null)
            maxPrice = BigDecimal.valueOf(Long.MAX_VALUE);
        if (statusList == null || statusList.isEmpty())
            statusList = List.of(ProductStatus.ACTIVE);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 10,
                Sort.by(direction, sortBy != null ? sortBy : "createdAt"));

        return productRepository
                .findByStoreAndStatusInAndNameContainingIgnoreCaseAndPriceBetween(
                        store, statusList, name, minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    // =========================================================
    // PRIVATE HELPERS
    // =========================================================
    private Store findStoreOrThrow(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store no encontrada"));
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    private void validateOwnership(Store store, String email) {
        if (!store.getOwner().getEmail().equals(email)) {
            throw new SecurityException("No tienes permisos para realizar esta acción");
        }
    }

    private void validateStoreApproved(Store store) {
        if (store.getStatus() != StoreStatus.APPROVED) {
            throw new IllegalStateException("La store no está aprobada");
        }
    }

    private void validateStorePublicAccess(Store store) {
        if (store.getStatus() != StoreStatus.APPROVED) {
            throw new IllegalStateException("La store no está disponible");
        }
    }

    // =========================================================
    // MAP — dentro de @Transactional, accede lazy sin problema
    // =========================================================
    private ProductResponse mapToResponse(Product product) {

        List<String> imageUrls = productImageRepository
                .findByProductAndStatus(product, ImageStatus.ACTIVE)
                .stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .status(product.getStatus())
                .featured(product.isFeatured())
                .mainImageUrl(product.getMainImageUrl())
                .imageUrls(imageUrls)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categoryIcon(product.getCategory() != null ? product.getCategory().getIcon() : null)
                .storeId(product.getStore().getId())
                .storeName(product.getStore().getName())
                .storeCity(product.getStore().getCity())
                .storeLogoUrl(product.getStore().getLogoUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}