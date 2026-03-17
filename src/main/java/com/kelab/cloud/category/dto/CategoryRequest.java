package com.kelab.cloud.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
    private String name;

    @Size(max = 200)
    private String description;

    @Size(max = 60)
    private String icon; // emoji o slug, ej: "📱" o "electronics"
}