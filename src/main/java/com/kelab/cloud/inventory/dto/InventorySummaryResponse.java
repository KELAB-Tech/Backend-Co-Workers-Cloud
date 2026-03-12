package com.kelab.cloud.inventory.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class InventorySummaryResponse {

    // Totales generales
    private long totalProducts;
    private long activeProducts;
    private long outOfStockProducts;
    private long inactiveProducts;

    // Movimientos
    private long totalEntradas; // unidades totales que entraron
    private long totalSalidas; // unidades totales que salieron

    // Alertas
    private long criticalAlerts; // productos con stock = 0
    private long lowStockAlerts; // productos bajo el mínimo configurado

    // Últimos movimientos (para el widget del dashboard)
    private List<MovementResponse> recentMovements;

    // Alertas activas
    private List<StockAlertResponse> alerts;
}