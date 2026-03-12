package com.kelab.cloud.inventory.repo;

import com.kelab.cloud.inventory.model.InventoryMovement;
import com.kelab.cloud.inventory.model.MovementType;
import com.kelab.cloud.marketplace.model.Product;
import com.kelab.cloud.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    // Todos los movimientos de un producto
    List<InventoryMovement> findByProductOrderByCreatedAtDesc(Product product);

    // Movimientos de todos los productos de una tienda
    @Query("""
                SELECT m FROM InventoryMovement m
                WHERE m.product.store = :store
                ORDER BY m.createdAt DESC
            """)
    List<InventoryMovement> findByStore(@Param("store") Store store);

    // Movimientos por tipo (ENTRADA / SALIDA / AJUSTE)
    @Query("""
                SELECT m FROM InventoryMovement m
                WHERE m.product.store = :store AND m.type = :type
                ORDER BY m.createdAt DESC
            """)
    List<InventoryMovement> findByStoreAndType(
            @Param("store") Store store,
            @Param("type") MovementType type);

    // Movimientos en rango de fechas
    @Query("""
                SELECT m FROM InventoryMovement m
                WHERE m.product.store = :store
                  AND m.createdAt BETWEEN :from AND :to
                ORDER BY m.createdAt DESC
            """)
    List<InventoryMovement> findByStoreAndDateRange(
            @Param("store") Store store,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // Últimos N movimientos de la tienda
    @Query("""
                SELECT m FROM InventoryMovement m
                WHERE m.product.store = :store
                ORDER BY m.createdAt DESC
                LIMIT 10
            """)
    List<InventoryMovement> findTop10ByStore(@Param("store") Store store);

    // Total unidades entrada/salida en tienda
    @Query("""
                SELECT COALESCE(SUM(m.quantity), 0) FROM InventoryMovement m
                WHERE m.product.store = :store AND m.type = :type
            """)
    Long sumQuantityByStoreAndType(
            @Param("store") Store store,
            @Param("type") MovementType type);
}