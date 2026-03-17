package com.kelab.cloud.user.service;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kelab.cloud.common.dto.PagedResponse;
import com.kelab.cloud.store.model.StoreStatus;
import com.kelab.cloud.user.dto.UserProfileResponse;
import com.kelab.cloud.user.dto.UserSelfUpdateRequest;
import com.kelab.cloud.user.dto.UserUpdateRequest;
import com.kelab.cloud.user.model.ActorType;
import com.kelab.cloud.user.model.Role;
import com.kelab.cloud.user.model.TipoPersona;
import com.kelab.cloud.user.model.User;
import com.kelab.cloud.user.repo.UserRepository;
import com.kelab.cloud.user.spec.UserSpecification;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==============================
    // GET MY PROFILE
    // ==============================
    public UserProfileResponse getMyProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return mapToResponse(user);
    }

    // ==============================
    // UPDATE MY PROFILE (solo nombre y email)
    // ==============================
    @Transactional
    public UserProfileResponse updateMyProfile(String email, UserSelfUpdateRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalStateException("Ese email ya está registrado por otro usuario");
            }
        }

        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());

        return mapToResponse(userRepository.save(user));
    }

    // ==============================
    // GET USER BY ID (ADMIN)
    // ==============================
    public UserProfileResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        return mapToResponse(user);
    }

    // ==============================
    // LISTAR USUARIOS (paginado + filtros)
    // ==============================
    public PagedResponse<UserProfileResponse> getAllUsers(
            String name,
            String email,
            TipoPersona tipoPersona,
            ActorType actorType,
            Boolean enabled,
            Pageable pageable) {

        Specification<User> spec = UserSpecification.filter(
                name, email, tipoPersona, actorType, enabled);

        Page<UserProfileResponse> page = userRepository
                .findAll(spec, pageable)
                .map(this::mapToResponse);

        return PagedResponse.of(page);
    }

    // ==============================
    // ACTUALIZAR USUARIO (ADMIN)
    // ==============================
    @Transactional
    public UserProfileResponse updateUser(Long id, UserUpdateRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setTipoPersona(request.getTipoPersona());
        user.setActorType(request.getActorType());

        return mapToResponse(userRepository.save(user));
    }

    // ==============================
    // SUSPEND USER
    // ==============================
    @Transactional
    public void suspendUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.isEnabled())
            throw new IllegalStateException("El usuario ya está suspendido");

        user.setEnabled(false);

        if (user.getStore() != null && user.getStore().getStatus() == StoreStatus.APPROVED) {
            user.getStore().setStatus(StoreStatus.SUSPENDED);
            user.getStore().setActive(false);
        }

        userRepository.save(user);
    }

    // ==============================
    // ACTIVATE USER
    // ==============================
    @Transactional
    public void activateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.isEnabled())
            throw new IllegalStateException("El usuario ya está activo");

        user.setEnabled(true);

        if (user.getStore() != null && user.getStore().getStatus() == StoreStatus.SUSPENDED) {
            user.getStore().setStatus(StoreStatus.APPROVED);
            user.getStore().setActive(true);
        }

        userRepository.save(user);
    }

    // ==============================
    // PRIVATE HELPERS
    // ==============================
    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .tipoPersona(user.getTipoPersona())
                .actorType(user.getActorType())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet()))
                .afiliado(user.isAfiliado())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}