package com.kelab.cloud.inventory.model;

import com.kelab.cloud.marketplace.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =============================
    // RELACIÓN (1 config por producto)
    // =============================

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    // =============================
    // CONFIGURACIÓN
    // =============================

    @Column(nullable = false)
    private Integer minStock; // umbral mínimo para alertas

    // =============================
    // AUDITORÍA
    // =============================

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String updatedBy;

    // =============================
    // LIFECYCLE
    // =============================

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }
}