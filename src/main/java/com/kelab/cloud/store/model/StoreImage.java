package com.kelab.cloud.store.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con la store
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 500)
    private String imageUrl; // URL de la imagen

    private String description; // opcional

    private boolean active = true; // para soft delete
}