package com.kelab.cloud.inventory.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class StockItemResponse {

    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer minStock;
    private String status; // ACTIVE | OUT_OF_STOCK | INACTIVE
}