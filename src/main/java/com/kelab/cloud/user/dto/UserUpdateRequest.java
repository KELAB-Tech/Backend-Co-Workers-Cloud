package com.kelab.cloud.user.dto;

import com.kelab.cloud.user.model.ActorType;
import com.kelab.cloud.user.model.TipoPersona;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene formato válido")
    private String email;

    @NotNull(message = "El tipo de persona es obligatorio")
    private TipoPersona tipoPersona;

    @NotNull(message = "El tipo de actor es obligatorio")
    private ActorType actorType;
}