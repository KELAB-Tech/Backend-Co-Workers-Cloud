package com.kelab.cloud.marketplace.controller;

import com.kelab.cloud.marketplace.dto.ProductRequest;
import com.kelab.cloud.marketplace.dto.ProductResponse;
import com.kelab.cloud.marketplace.model.ProductStatus;
import com.kelab.cloud.marketplace.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

        private final ProductService productService;

        // =========================================================
        // MARKETPLACE GLOBAL
        // =========================================================

        /**
         * GET /api/products/marketplace
         * Búsqueda global paginada — el frontend del marketplace consume este endpoint.
         * Parámetros opcionales: name, categoryId, minPrice, maxPrice, city,
         * page, size, sortBy (price|createdAt), sortDir (asc|desc)
         */
        @GetMapping("/marketplace")
        public ResponseEntity<Page<ProductResponse>> getMarketplaceProducts(
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @RequestParam(required = false) String city,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer size,
                        @RequestParam(required = false) String sortBy,
                        @RequestParam(required = false) String sortDir) {

                return ResponseEntity.ok(
                                productService.getMarketplaceProducts(
                                                name, categoryId, minPrice, maxPrice,
                                                city, page, size, sortBy, sortDir));
        }

        /**
         * GET /api/products/marketplace/featured
         * Productos destacados globales para el banner del marketplace.
         */
        @GetMapping("/marketplace/featured")
        public ResponseEntity<List<ProductResponse>> getGlobalFeaturedProducts() {
                return ResponseEntity.ok(productService.getGlobalFeaturedProducts());
        }

        /**
         * GET /api/products/category/{categoryId}
         * Todos los productos activos de una categoría (útil para páginas de
         * categoría).
         */
        @GetMapping("/category/{categoryId}")
        public ResponseEntity<List<ProductResponse>> getProductsByCategory(
                        @PathVariable Long categoryId) {
                return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
        }

        // =========================================================
        // PUBLIC ENDPOINTS (por store)
        // =========================================================

        @GetMapping("/{productId}")
        public ResponseEntity<ProductResponse> getProductById(
                        @PathVariable Long productId) {
                return ResponseEntity.ok(productService.getProductById(productId));
        }

        @GetMapping("/store/{storeId}/featured")
        public ResponseEntity<List<ProductResponse>> getFeaturedProducts(
                        @PathVariable Long storeId) {
                return ResponseEntity.ok(productService.getFeaturedProducts(storeId));
        }

        @GetMapping("/store/{storeId}/out-of-stock")
        public ResponseEntity<List<ProductResponse>> getOutOfStockProducts(
                        @PathVariable Long storeId) {
                return ResponseEntity.ok(productService.getOutOfStockProducts(storeId));
        }

        @GetMapping("/store/{storeId}/search")
        public ResponseEntity<Page<ProductResponse>> searchProducts(
                        @PathVariable Long storeId,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @RequestParam(required = false) List<ProductStatus> status,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer size,
                        @RequestParam(required = false) String sortBy,
                        @RequestParam(required = false) String sortDir) {

                return ResponseEntity.ok(
                                productService.filterProductsPaged(
                                                storeId, name, minPrice, maxPrice,
                                                status, page, size, sortBy, sortDir));
        }

        // =========================================================
        // OWNER ENDPOINTS
        // =========================================================

        @PostMapping("/store/{storeId}")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<ProductResponse> createProduct(
                        @PathVariable Long storeId,
                        @Valid @RequestBody ProductRequest request,
                        Principal principal) {

                ProductResponse response = productService.createProduct(storeId, request, principal.getName());
                return ResponseEntity.status(201).body(response);
        }

        @PutMapping("/{productId}")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<ProductResponse> updateProduct(
                        @PathVariable Long productId,
                        @Valid @RequestBody ProductRequest request,
                        Principal principal) {

                return ResponseEntity.ok(
                                productService.updateProduct(productId, request, principal.getName()));
        }

        @DeleteMapping("/{productId}")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<Void> deleteProduct(
                        @PathVariable Long productId,
                        Principal principal) {

                productService.deleteProduct(productId, principal.getName());
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/store/{storeId}/owner")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<List<ProductResponse>> getAllProductsForOwner(
                        @PathVariable Long storeId,
                        Principal principal) {

                return ResponseEntity.ok(
                                productService.getAllProductsByStoreForOwner(storeId, principal.getName()));
        }

        @GetMapping("/my-store")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<List<ProductResponse>> getMyStoreProducts(
                        Principal principal) {

                return ResponseEntity.ok(
                                productService.getMyStoreProducts(principal.getName()));
        }

        // =========================================================
        // IMAGE MANAGEMENT (OWNER)
        // =========================================================

        @PostMapping("/{productId}/images")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<Void> addImage(
                        @PathVariable Long productId,
                        @RequestParam String imageUrl,
                        Principal principal) {

                productService.addProductImage(productId, imageUrl, principal.getName());
                return ResponseEntity.status(201).build();
        }

        @DeleteMapping("/images/{imageId}")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<Void> deleteImage(
                        @PathVariable Long imageId,
                        Principal principal) {

                productService.deleteProductImage(imageId, principal.getName());
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/{productId}/images")
        public ResponseEntity<List<String>> getImages(
                        @PathVariable Long productId) {

                return ResponseEntity.ok(productService.getProductImages(productId));
        }

        // =========================================================
        // ADMIN MODERATION
        // =========================================================

        @PatchMapping("/admin/{productId}/deactivate")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> adminDeactivateProduct(@PathVariable Long productId) {
                productService.adminDeactivateProduct(productId);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/admin/{productId}/activate")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> adminActivateProduct(@PathVariable Long productId) {
                productService.adminActivateProduct(productId);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/admin/{productId}/toggle-featured")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> adminToggleFeatured(@PathVariable Long productId) {
                productService.adminToggleFeatured(productId);
                return ResponseEntity.noContent().build();
        }
}