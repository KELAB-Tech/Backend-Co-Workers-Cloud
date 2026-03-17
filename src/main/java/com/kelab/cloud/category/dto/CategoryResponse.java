package com.kelab.cloud.category.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private boolean active;
    private long productCount; // cuántos productos tiene esta categoría (para el frontend)
}