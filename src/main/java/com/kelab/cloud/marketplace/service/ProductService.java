package com.kelab.cloud.marketplace.service;

import com.kelab.cloud.marketplace.dto.ProductRequest;
import com.kelab.cloud.marketplace.dto.ProductResponse;
import com.kelab.cloud.marketplace.model.*;
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

    // =========================================================
    // ADMIN - List All Products (for moderation)
    // =========================================================
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
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

        Product product = Product.builder()
                .name(trimmedName)
                .description(request.getDescription().trim())
                .price(request.getPrice())
                .stock(request.getStock())
                .mainImageUrl(request.getMainImageUrl().trim())
                .store(store)
                .createdBy(email)
                .build();

        return mapToResponse(productRepository.save(product));
    }

    // =========================================================
    // PUBLIC METHODS (SOLO STORE APPROVED)
    // =========================================================

    public List<ProductResponse> getProductsByStore(Long storeId) {

        Store store = findStoreOrThrow(storeId);
        validateStorePublicAccess(store);

        return productRepository
                .findByStoreAndStatus(store, ProductStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse getProductById(Long productId) {

        Product product = findProductOrThrow(productId);
        validateStorePublicAccess(product.getStore());

        return mapToResponse(product);
    }

    public List<ProductResponse> getFeaturedProducts(Long storeId) {

        Store store = findStoreOrThrow(storeId);
        validateStorePublicAccess(store);

        return productRepository
                .findByStoreAndFeaturedTrueAndStatus(store, ProductStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProductResponse> getOutOfStockProducts(Long storeId) {

        Store store = findStoreOrThrow(storeId);
        validateStorePublicAccess(store);

        return productRepository
                .findByStoreAndStatus(store, ProductStatus.OUT_OF_STOCK)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // OWNER VIEW — con storeId en URL (mantener para compatibilidad)
    // =========================================================

    public List<ProductResponse> getAllProductsByStoreForOwner(Long storeId, String email) {

        Store store = findStoreOrThrow(storeId);
        validateOwnership(store, email);

        return productRepository.findByStore(store)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // OWNER VIEW — sin storeId, se extrae del JWT ✅ NUEVO
    // =========================================================

    public List<ProductResponse> getMyStoreProducts(String email) {

        Store store = storeRepository.findByOwnerEmail(email)
                .orElseThrow(() -> new IllegalStateException("No tienes ninguna tienda registrada"));

        return productRepository.findByStore(store)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // UPDATE (OWNER)
    // =========================================================

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest request, String email) {

        Product product = findProductOrThrow(productId);
        validateOwnership(product.getStore(), email);

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setMainImageUrl(request.getMainImageUrl().trim());
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

        Product product = findProductOrThrow(productId);
        product.deactivate();
    }

    @Transactional
    public void adminActivateProduct(Long productId) {

        Product product = findProductOrThrow(productId);

        if (product.getStock() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else {
            product.setStatus(ProductStatus.ACTIVE);
        }
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

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl.trim())
                .build();

        productImageRepository.save(image);
    }

    public List<String> getProductImages(Long productId) {

        Product product = findProductOrThrow(productId);

        return productImageRepository
                .findByProductAndStatus(product, ImageStatus.ACTIVE)
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();
    }

    @Transactional
    public void deleteProductImage(Long imageId, String email) {

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada"));

        validateOwnership(image.getProduct().getStore(), email);

        image.deactivate();
    }

    // =========================================================
    // PAGINACIÓN + FILTROS (PÚBLICO)
    // =========================================================

    public Page<ProductResponse> filterProductsPaged(
            Long storeId,
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            List<ProductStatus> statusList,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir) {

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

        Page<Product> pageResult = productRepository.findByStoreAndStatusInAndNameContainingIgnoreCaseAndPriceBetween(
                store, statusList, name, minPrice, maxPrice, pageable);

        return pageResult.map(this::mapToResponse);
    }

    // =========================================================
    // INTERNAL VALIDATIONS
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
                .mainImageUrl(product.getMainImageUrl())
                .imageUrls(imageUrls)
                .storeId(product.getStore().getId())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}