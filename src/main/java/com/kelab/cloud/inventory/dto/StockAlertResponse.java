package com.kelab.cloud.inventory.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class StockAlertResponse {

    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer minStock;
    private String level; // "CRITICAL" (stock=0) | "LOW" (stock < minStock)
}