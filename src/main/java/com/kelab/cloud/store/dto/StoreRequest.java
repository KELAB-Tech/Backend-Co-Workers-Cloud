package com.kelab.cloud.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StoreRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 1000, message = "La descripción debe tener mínimo 10 caracteres")
    private String description;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+\\- ]{7,15}$", message = "Teléfono inválido")
    private String phone;

    @NotBlank(message = "La ciudad es obligatoria")
    private String city;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @Size(max = 500, message = "URL del logo demasiado larga")
    private String logoUrl;
}