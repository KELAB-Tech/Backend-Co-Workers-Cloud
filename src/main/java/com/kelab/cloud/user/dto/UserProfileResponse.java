package com.kelab.cloud.user.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.kelab.cloud.user.model.ActorType;
import com.kelab.cloud.user.model.TipoPersona;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;
    private TipoPersona tipoPersona;
    private ActorType actorType;
    private Set<String> roles;

    // ── Campos nuevos ──────────────────────────────
    private boolean afiliado;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}