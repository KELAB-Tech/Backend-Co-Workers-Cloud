package com.kelab.cloud.inventory.dto;

import com.kelab.cloud.inventory.model.MovementType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MovementResponse {

    private Long id;
    private Long productId;
    private String productName;
    private MovementType type;
    private Integer quantity;
    private Integer stockBefore;
    private Integer stockAfter;
    private String reason;
    private String createdBy;
    private LocalDateTime createdAt;
}