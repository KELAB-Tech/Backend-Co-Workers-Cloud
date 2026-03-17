package com.kelab.cloud.marketplace.dto;

import com.kelab.cloud.marketplace.model.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private ProductStatus status;
    private boolean featured;
    private String mainImageUrl;
    private List<String> imageUrls;

    // Categoría (puede ser null si no fue asignada)
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;

    // Store info básica (útil para el marketplace sin tener que llamar otro
    // endpoint)
    private Long storeId;
    private String storeName;
    private String storeCity;
    private String storeLogoUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}