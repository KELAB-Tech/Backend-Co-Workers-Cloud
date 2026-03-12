package com.kelab.cloud.marketplace.repo;

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

        // Listar productos por tienda
        List<Product> findByStore(Store store);

        // Verificar si existe un producto con el mismo nombre en la misma tienda
        boolean existsByStoreAndNameIgnoreCase(Store store, String name);

        // Búsqueda avanzada
        List<Product> findByStoreAndNameContainingIgnoreCaseAndPriceBetweenAndStatusIn(
                        Store store,
                        String name,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        List<ProductStatus> statusList);

        // Versión paginada
        Page<Product> findByStoreAndStatusInAndNameContainingIgnoreCaseAndPriceBetween(
                        Store store,
                        List<ProductStatus> statusList,
                        String name,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        Pageable pageable);

        // Productos destacados
        List<Product> findByStoreAndFeaturedTrueAndStatus(
                        Store store,
                        ProductStatus status);

        // Productos por estado
        List<Product> findByStoreAndStatus(Store store, ProductStatus status);

        // ==============================
        // ADMIN DASHBOARD
        // ==============================

        // Total productos por estado
        long countByStatus(ProductStatus status);

        // Total productos por múltiples estados
        long countByStatusIn(List<ProductStatus> statuses);

        // Total productos por tienda
        long countByStore(Store store);

        // Total productos por tienda y estado
        long countByStoreAndStatus(Store store, ProductStatus status);

        // Total productos destacados
        long countByFeaturedTrue();

        // Últimos productos añadidos
        List<Product> findTop5ByOrderByCreatedAtDesc();

        // Productos con precio entre rango
        List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

        // Productos con nombre que contiene texto
        List<Product> findByNameContainingIgnoreCase(String name);

        // Productos con precio mayor a
        List<Product> findByPriceGreaterThan(BigDecimal price);

        // Productos con precio menor a
        List<Product> findByPriceLessThan(BigDecimal price);

        // productos sin stock
        long countByStock(Integer stock);

        // productos con stock menor a X
        long countByStockLessThan(Integer stock);

        // productos creados hoy
        long countByCreatedAtAfter(java.time.LocalDateTime date);

}