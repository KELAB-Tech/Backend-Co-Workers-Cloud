package com.kelab.cloud.auth.dto;

import com.kelab.cloud.user.model.ActorType;
import com.kelab.cloud.user.model.TipoPersona;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    private String password;

    @NotNull
    private TipoPersona tipoPersona;

    @NotNull
    private ActorType actorType;
}
