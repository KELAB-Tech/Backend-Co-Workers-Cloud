package com.kelab.cloud.inventory.service;

import com.kelab.cloud.inventory.dto.*;
import com.kelab.cloud.inventory.model.*;
import com.kelab.cloud.inventory.repo.*;
import com.kelab.cloud.marketplace.model.Product;
import com.kelab.cloud.marketplace.model.ProductStatus;
import com.kelab.cloud.marketplace.repo.ProductRepository;
import com.kelab.cloud.store.model.Store;
import com.kelab.cloud.store.repo.StoreRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final InventoryMovementRepository movementRepository;
    private final StockConfigRepository stockConfigRepository;

    // =========================================================
    // RESUMEN GENERAL del inventario (dashboard)
    // =========================================================

    public InventorySummaryResponse getSummary(String email) {

        Store store = findStoreOrThrow(email);
        List<Product> products = productRepository.findByStore(store);

        long total = products.size();
        long active = products.stream().filter(p -> p.getStatus() == ProductStatus.ACTIVE).count();
        long outOfStock = products.stream().filter(p -> p.getStatus() == ProductStatus.OUT_OF_STOCK).count();
        long inactive = products.stream().filter(p -> p.getStatus() == ProductStatus.INACTIVE).count();

        long entradas = movementRepository.sumQuantityByStoreAndType(store, MovementType.ENTRADA);
        long salidas = movementRepository.sumQuantityByStoreAndType(store, MovementType.SALIDA);

        List<StockAlertResponse> alerts = buildAlerts(store, products);
        long criticalAlerts = alerts.stream().filter(a -> "CRITICAL".equals(a.getLevel())).count();
        long lowStockAlerts = alerts.stream().filter(a -> "LOW".equals(a.getLevel())).count();

        List<MovementResponse> recent = movementRepository
                .findTop10ByStore(store)
                .stream()
                .map(this::mapMovement)
                .toList();

        return InventorySummaryResponse.builder()
                .totalProducts(total)
                .activeProducts(active)
                .outOfStockProducts(outOfStock)
                .inactiveProducts(inactive)
                .totalEntradas(entradas)
                .totalSalidas(salidas)
                .criticalAlerts(criticalAlerts)
                .lowStockAlerts(lowStockAlerts)
                .recentMovements(recent)
                .alerts(alerts)
                .build();
    }

    // =========================================================
    // REGISTRAR MOVIMIENTO (entrada / salida / ajuste)
    // =========================================================

    @Transactional
    public MovementResponse registerMovement(MovementRequest request, String email) {

        Store store = findStoreOrThrow(email);
        Product product = findProductOrThrow(request.getProductId());
        validateOwnership(product.getStore(), store);

        int stockBefore = product.getStock();
        int stockAfter;

        switch (request.getType()) {
            case ENTRADA -> stockAfter = stockBefore + request.getQuantity();
            case SALIDA -> {
                if (request.getQuantity() > stockBefore) {
                    throw new IllegalStateException(
                            "Stock insuficiente. Disponible: " + stockBefore + ", solicitado: "
                                    + request.getQuantity());
                }
                stockAfter = stockBefore - request.getQuantity();
            }
            case AJUSTE -> stockAfter = request.getQuantity(); // ajuste directo al valor
            default -> throw new IllegalArgumentException("Tipo de movimiento inválido");
        }

        // Actualizar stock en el producto (reutiliza el método de dominio)
        product.updateStock(stockAfter);

        // Guardar movimiento
        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .type(request.getType())
                .quantity(request.getQuantity())
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .reason(request.getReason())
                .createdBy(email)
                .build();

        return mapMovement(movementRepository.save(movement));
    }

    // =========================================================
    // HISTORIAL DE MOVIMIENTOS de la tienda
    // =========================================================

    public List<MovementResponse> getMovements(String email) {

        Store store = findStoreOrThrow(email);

        return movementRepository.findByStore(store)
                .stream()
                .map(this::mapMovement)
                .toList();
    }

    // Movimientos filtrados por tipo
    public List<MovementResponse> getMovementsByType(String email, MovementType type) {

        Store store = findStoreOrThrow(email);

        return movementRepository.findByStoreAndType(store, type)
                .stream()
                .map(this::mapMovement)
                .toList();
    }

    // Movimientos de un producto específico
    public List<MovementResponse> getMovementsByProduct(Long productId, String email) {

        Store store = findStoreOrThrow(email);
        Product product = findProductOrThrow(productId);
        validateOwnership(product.getStore(), store);

        return movementRepository.findByProductOrderByCreatedAtDesc(product)
                .stream()
                .map(this::mapMovement)
                .toList();
    }

    // =========================================================
    // ALERTAS DE STOCK
    // =========================================================

    public List<StockAlertResponse> getAlerts(String email) {

        Store store = findStoreOrThrow(email);
        List<Product> products = productRepository.findByStore(store);

        return buildAlerts(store, products);
    }

    // =========================================================
    // CONFIGURAR STOCK MÍNIMO por producto
    // =========================================================

    @Transactional
    public void setMinStock(Long productId, StockConfigRequest request, String email) {

        Store store = findStoreOrThrow(email);
        Product product = findProductOrThrow(productId);
        validateOwnership(product.getStore(), store);

        StockConfig config = stockConfigRepository.findByProduct(product)
                .orElse(StockConfig.builder().product(product).build());

        config.setMinStock(request.getMinStock());
        config.setUpdatedBy(email);

        stockConfigRepository.save(config);
    }

    // =========================================================
    // STOCK ACTUAL de todos los productos de la tienda
    // =========================================================

    public List<StockItemResponse> getCurrentStock(String email) {

        Store store = findStoreOrThrow(email);
        List<Product> products = productRepository.findByStore(store);

        // ✅ REEMPLAZA este bloque en getCurrentStock()
        return products.stream().map(p -> {
            int minStock = stockConfigRepository.findByProduct(p)
                    .map(StockConfig::getMinStock)
                    .orElse(0);

            return StockItemResponse.builder()
                    .productId(p.getId())
                    .productName(p.getName())
                    .currentStock(p.getStock())
                    .minStock(minStock)
                    .status(p.getStatus().name())
                    .mainImageUrl(p.getMainImageUrl()) // ← NUEVO
                    .categoryIcon(p.getCategory() != null ? p.getCategory().getIcon() : null) // ← NUEVO
                    .build();
        }).toList();
    }

    // =========================================================
    // INTERNAL HELPERS
    // =========================================================

    private Store findStoreOrThrow(String email) {
        return storeRepository.findByOwnerEmail(email)
                .orElseThrow(() -> new IllegalStateException("No tienes ninguna tienda registrada"));
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    private void validateOwnership(Store productStore, Store ownerStore) {
        if (!productStore.getId().equals(ownerStore.getId())) {
            throw new SecurityException("Este producto no pertenece a tu tienda");
        }
    }

    private List<StockAlertResponse> buildAlerts(Store store, List<Product> products) {

        List<StockConfig> configs = stockConfigRepository.findByStore(store);

        return products.stream()
                .filter(p -> p.getStatus() != ProductStatus.INACTIVE)
                .filter(p -> {
                    int minStock = configs.stream()
                            .filter(c -> c.getProduct().getId().equals(p.getId()))
                            .findFirst()
                            .map(StockConfig::getMinStock)
                            .orElse(0);
                    return p.getStock() == 0 || p.getStock() <= minStock;
                })
                .map(p -> {
                    int minStock = configs.stream()
                            .filter(c -> c.getProduct().getId().equals(p.getId()))
                            .findFirst()
                            .map(StockConfig::getMinStock)
                            .orElse(0);

                    String level = p.getStock() == 0 ? "CRITICAL" : "LOW";

                    return StockAlertResponse.builder()
                            .productId(p.getId())
                            .productName(p.getName())
                            .currentStock(p.getStock())
                            .minStock(minStock)
                            .level(level)
                            .build();
                })
                .toList();
    }

    private MovementResponse mapMovement(InventoryMovement m) {
        return MovementResponse.builder()
                .id(m.getId())
                .productId(m.getProduct().getId())
                .productName(m.getProduct().getName())
                .type(m.getType())
                .quantity(m.getQuantity())
                .stockBefore(m.getStockBefore())
                .stockAfter(m.getStockAfter())
                .reason(m.getReason())
                .createdBy(m.getCreatedBy())
                .createdAt(m.getCreatedAt())
                .build();
    }
}