package com.kelab.cloud.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.kelab.cloud.marketplace.model.ProductStatus;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private ProductStatus status;

    private String mainImageUrl;

    private List<String> imageUrls;

    private Long storeId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}