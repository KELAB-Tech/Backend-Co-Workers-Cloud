package com.kelab.cloud.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStoreProductSummaryResponse {

    private Long storeId;
    private String storeName;
    private long productCount;

}