package com.kelab.cloud.user.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kelab.cloud.admin.service.AdminService;
import com.kelab.cloud.user.dto.UserProfileResponse;
import com.kelab.cloud.user.dto.UserSelfUpdateRequest;
import com.kelab.cloud.user.dto.UserUpdateRequest;
import com.kelab.cloud.user.model.Role;
import com.kelab.cloud.user.model.User;
import com.kelab.cloud.user.repo.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final AdminService adminService;
    private final UserRepository userRepository;

    public UserService(AdminService adminService, UserRepository userRepository) {
        this.adminService = adminService;
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

        // Validar que el nuevo email no esté tomado por otro usuario
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
    // LISTAR TODOS LOS USUARIOS (ADMIN)
    // ==============================
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ==============================
    // ACTUALIZAR USUARIO (ADMIN — puede cambiar tipoPersona y actorType)
    // ==============================
    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        existingUser.setName(request.getName());
        existingUser.setEmail(request.getEmail());
        existingUser.setTipoPersona(request.getTipoPersona());
        existingUser.setActorType(request.getActorType());

        return userRepository.save(existingUser);
    }

    // ==============================
    // ADMIN - Suspend / Activate User
    // ==============================
    @Transactional
    public void suspendUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.isEnabled()) {
            throw new RuntimeException("El usuario ya está suspendido");
        }

        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long id) {
        adminService.activateUser(id);
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