// ─────────────────────────────────────────────
// MovementRequest.java
// ─────────────────────────────────────────────
package com.kelab.cloud.inventory.dto;

import com.kelab.cloud.inventory.model.MovementType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private MovementType type; // ENTRADA | SALIDA | AJUSTE

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;

    @Size(max = 300, message = "El motivo no puede superar 300 caracteres")
    private String reason;
}