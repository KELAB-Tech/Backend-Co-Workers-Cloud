package com.kelab.cloud.marketplace.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 1000, message = "Máximo 1000 caracteres")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotBlank(message = "La imagen principal es obligatoria")
    private String mainImageUrl;

    // ← NUEVO: opcional, el usuario puede no asignar categoría
    private Long categoryId;
}