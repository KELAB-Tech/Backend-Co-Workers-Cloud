package com.kelab.cloud.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Lo que el usuario puede editar de sí mismo.
 * tipoPersona y actorType son datos estructurales — solo un admin los cambia.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSelfUpdateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    // Opcional: teléfono de contacto (agrégalo al modelo si lo quieres guardar)
    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String phone;
}