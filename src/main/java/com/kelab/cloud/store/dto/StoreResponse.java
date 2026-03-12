package com.kelab.cloud.store.dto;

import com.kelab.cloud.store.model.StoreStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreResponse {

    private Long id;
    private String name;
    private String description;
    private String phone;
    private String city;
    private String address;
    private String logoUrl;

    private StoreStatus status;
    private boolean active;
}