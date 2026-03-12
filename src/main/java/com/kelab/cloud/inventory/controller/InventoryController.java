package com.kelab.cloud.inventory.controller;

import com.kelab.cloud.inventory.dto.*;
import com.kelab.cloud.inventory.model.MovementType;
import com.kelab.cloud.inventory.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class InventoryController {

    private final InventoryService inventoryService;

    // =========================================================
    // RESUMEN GENERAL — para el dashboard de inventario
    // GET /api/inventory/summary
    // =========================================================
    @GetMapping("/summary")
    public ResponseEntity<InventorySummaryResponse> getSummary(Principal principal) {
        return ResponseEntity.ok(
                inventoryService.getSummary(principal.getName()));
    }

    // =========================================================
    // STOCK ACTUAL — tabla con stock actual vs mínimo
    // GET /api/inventory/stock
    // =========================================================
    @GetMapping("/stock")
    public ResponseEntity<List<StockItemResponse>> getCurrentStock(Principal principal) {
        return ResponseEntity.ok(
                inventoryService.getCurrentStock(principal.getName()));
    }

    // =========================================================
    // ALERTAS — productos bajo el mínimo o sin stock
    // GET /api/inventory/alerts
    // =========================================================
    @GetMapping("/alerts")
    public ResponseEntity<List<StockAlertResponse>> getAlerts(Principal principal) {
        return ResponseEntity.ok(
                inventoryService.getAlerts(principal.getName()));
    }

    // =========================================================
    // MOVIMIENTOS — historial completo
    // GET /api/inventory/movements
    // GET /api/inventory/movements?type=ENTRADA
    // =========================================================
    @GetMapping("/movements")
    public ResponseEntity<List<MovementResponse>> getMovements(
            @RequestParam(required = false) MovementType type,
            Principal principal) {

        if (type != null) {
            return ResponseEntity.ok(
                    inventoryService.getMovementsByType(principal.getName(), type));
        }

        return ResponseEntity.ok(
                inventoryService.getMovements(principal.getName()));
    }

    // Movimientos de un producto específico
    // GET /api/inventory/movements/product/{productId}
    @GetMapping("/movements/product/{productId}")
    public ResponseEntity<List<MovementResponse>> getMovementsByProduct(
            @PathVariable Long productId,
            Principal principal) {

        return ResponseEntity.ok(
                inventoryService.getMovementsByProduct(productId, principal.getName()));
    }

    // =========================================================
    // REGISTRAR MOVIMIENTO — entrada, salida o ajuste
    // POST /api/inventory/movement
    // =========================================================
    @PostMapping("/movement")
    public ResponseEntity<MovementResponse> registerMovement(
            @Valid @RequestBody MovementRequest request,
            Principal principal) {

        return ResponseEntity.status(201).body(
                inventoryService.registerMovement(request, principal.getName()));
    }

    // =========================================================
    // CONFIGURAR STOCK MÍNIMO de un producto
    // PUT /api/inventory/product/{productId}/min-stock
    // =========================================================
    @PutMapping("/product/{productId}/min-stock")
    public ResponseEntity<Void> setMinStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockConfigRequest request,
            Principal principal) {

        inventoryService.setMinStock(productId, request, principal.getName());
        return ResponseEntity.noContent().build();
    }
}