package com.kelab.cloud.inventory.model;

import com.kelab.cloud.marketplace.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =============================
    // RELACIÓN
    // =============================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // =============================
    // DATOS DEL MOVIMIENTO
    // =============================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    @Column(nullable = false)
    private Integer quantity; // siempre positivo

    @Column(nullable = false)
    private Integer stockBefore; // stock antes del movimiento

    @Column(nullable = false)
    private Integer stockAfter; // stock después del movimiento

    @Column(length = 300)
    private String reason; // motivo del movimiento

    // =============================
    // AUDITORÍA
    // =============================

    @Column(nullable = false, updatable = false)
    private String createdBy; // email del usuario

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // =============================
    // LIFECYCLE
    // =============================

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}