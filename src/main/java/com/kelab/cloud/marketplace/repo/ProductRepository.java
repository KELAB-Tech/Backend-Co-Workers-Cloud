package com.kelab.cloud.marketplace.repo;

import com.kelab.cloud.category.model.Category;
import com.kelab.cloud.marketplace.model.Product;
import com.kelab.cloud.marketplace.model.ProductStatus;
import com.kelab.cloud.store.model.Store;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

        // ==============================
        // STORE PRODUCTS
        // ==============================

        List<Product> findByStore(Store store);

        boolean existsByStoreAndNameIgnoreCase(Store store, String name);

        List<Product> findByStoreAndNameContainingIgnoreCaseAndPriceBetweenAndStatusIn(
                        Store store,
                        String name,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        List<ProductStatus> statusList);

        Page<Product> findByStoreAndStatusInAndNameContainingIgnoreCaseAndPriceBetween(
                        Store store,
                        List<ProductStatus> statusList,
                        String name,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        Pageable pageable);

        List<Product> findByStoreAndFeaturedTrueAndStatus(Store store, ProductStatus status);

        List<Product> findByStoreAndStatus(Store store, ProductStatus status);

        // ==============================
        // MARKETPLACE GLOBAL — nuevas queries
        // ==============================

        // Featured globales
        List<Product> findByFeaturedTrueAndStatus(ProductStatus status);

        // Marketplace sin filtros de tienda — solo por status, nombre y precio
        Page<Product> findByStatusAndNameContainingIgnoreCaseAndPriceBetween(
                        ProductStatus status,
                        String name,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        Pageable pageable);

        // Marketplace filtrado por categoría
        Page<Product> findByStatusAndCategoryAndNameContainingIgnoreCaseAndPriceBetween(
                        ProductStatus status,
                        Category category,
                        String name,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        Pageable pageable);

        // Marketplace filtrado por ciudad
        Page<Product> findByStatusAndStore_CityIgnoreCaseAndNameContainingIgnoreCaseAndPriceBetween(
                        ProductStatus status,
                        String city,
                        String name,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        Pageable pageable);

        // Marketplace filtrado por categoría + ciudad
        Page<Product> findByStatusAndCategoryAndStore_CityIgnoreCaseAndNameContainingIgnoreCaseAndPriceBetween(
                        ProductStatus status,
                        Category category,
                        String city,
                        String name,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        Pageable pageable);

        // Productos por categoría (lista simple)
        List<Product> findByCategoryAndStatus(Category category, ProductStatus status);

        // ==============================
        // ADMIN DASHBOARD
        // ==============================

        long countByStatus(ProductStatus status);

        long countByStatusIn(List<ProductStatus> statuses);

        long countByStore(Store store);

        long countByStoreAndStatus(Store store, ProductStatus status);

        long countByFeaturedTrue();

        List<Product> findTop5ByOrderByCreatedAtDesc();

        List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

        List<Product> findByNameContainingIgnoreCase(String name);

        List<Product> findByPriceGreaterThan(BigDecimal price);

        List<Product> findByPriceLessThan(BigDecimal price);

        long countByStock(Integer stock);

        long countByStockLessThan(Integer stock);

        long countByCreatedAtAfter(java.time.LocalDateTime date);

        long countByCategoryAndStatus(Category category, ProductStatus status);
}