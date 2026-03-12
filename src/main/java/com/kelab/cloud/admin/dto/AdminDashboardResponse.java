package com.kelab.cloud.admin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalUsers;
    private long activeUsers;
    private long suspendedUsers;

    private long totalStores;
    private long approvedStores;
    private long pendingStores;
    private long suspendedStores;

    private long totalProducts;
    private long activeProducts;
    private long inactiveProducts;
    private long featuredProducts;
    private long outOfStockProducts;
    private long lowStockProducts;
    private long productsCreatedToday;

    private List<AdminProductSummaryResponse> lastProducts;
    private List<AdminStoreProductSummaryResponse> topStores;
}