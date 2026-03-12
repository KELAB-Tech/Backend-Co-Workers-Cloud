package com.kelab.cloud.store.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreImageResponse {

    private Long id;
    private String imageUrl;
    private String description;
}