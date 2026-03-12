package com.kelab.cloud.auth.dto;

import java.util.Set;

import com.kelab.cloud.user.model.ActorType;
import com.kelab.cloud.user.model.TipoPersona;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private String email;
    private Set<String> roles;
    private String message;
    private ActorType actorType;
    private TipoPersona tipoPersona;
}
