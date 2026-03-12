package com.kelab.cloud.user.dto;

import com.kelab.cloud.user.model.TipoPersona;
import com.kelab.cloud.user.model.ActorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private String name;
    private String email;
    private TipoPersona tipoPersona;
    private ActorType actorType;
}