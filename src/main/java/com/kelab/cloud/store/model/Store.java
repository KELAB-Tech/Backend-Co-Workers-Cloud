package com.kelab.cloud.store.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kelab.cloud.marketplace.model.Product;
import com.kelab.cloud.user.model.User;

@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String phone;
    private String city;
    private String address;
    private String logoUrl;

    @Column(nullable = false)
    private boolean active = true;

    // Dueño
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private User owner;

    // Productos
    @JsonIgnore
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Product> products;

    // Imágenes de la tienda
    @JsonIgnore
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreImage> images;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}