package com.kelab.cloud.marketplace.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.kelab.cloud.category.model.Category;
import com.kelab.cloud.store.model.Store;

@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(name = "uk_store_product_name", columnNames = { "store_id", "name" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =============================
    // BASIC INFO
    // =============================

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, length = 500)
    private String mainImageUrl;

    // =============================
    // CATEGORY ← NUEVO
    // =============================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // =============================
    // STATUS
    // =============================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(nullable = false)
    private boolean featured;

    // =============================
    // RELATIONS
    // =============================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    // =============================
    // AUDIT
    // =============================

    @Column(nullable = false, updatable = false)
    private String createdBy;

    private String updatedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // =============================
    // LIFECYCLE
    // =============================

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.stock == null) {
            throw new IllegalStateException("El stock no puede ser null");
        }

        this.status = (this.stock == 0)
                ? ProductStatus.OUT_OF_STOCK
                : ProductStatus.ACTIVE;

        if (!this.featured) {
            this.featured = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();

        if (this.stock == null) {
            throw new IllegalStateException("El stock no puede ser null");
        }

        if (this.status != ProductStatus.INACTIVE) {
            this.status = (this.stock == 0)
                    ? ProductStatus.OUT_OF_STOCK
                    : ProductStatus.ACTIVE;
        }
    }

    // =============================
    // DOMAIN METHODS
    // =============================

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public void updateStock(Integer newStock) {
        if (newStock == null || newStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        this.stock = newStock;
    }

    public boolean isAvailable() {
        return this.status == ProductStatus.ACTIVE;
    }
}