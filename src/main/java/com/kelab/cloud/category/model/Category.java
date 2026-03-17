package com.kelab.cloud.category.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.kelab.cloud.marketplace.model.Product;

@Entity
@Table(name = "categories")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(length = 200)
    private String description;

    // ícono tipo "emoji" o slug para el frontend (ej: "electronics", "🛍️")
    @Column(length = 60)
    private String icon;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // Relación inversa (no es cargada eagerly para evitar N+1)
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products;
}