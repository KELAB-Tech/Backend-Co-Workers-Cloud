package com.kelab.cloud.admin.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminProductSummaryResponse {

    private Long id;
    private String name;
    private String storeName;
    private BigDecimal price;
}