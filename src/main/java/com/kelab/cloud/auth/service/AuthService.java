package com.kelab.cloud.auth.service;

import com.kelab.cloud.auth.dto.AuthResponse;
import com.kelab.cloud.auth.dto.LoginRequest;
import com.kelab.cloud.auth.dto.RegisterRequest;
import com.kelab.cloud.security.JwtService;
import com.kelab.cloud.user.model.Role;
import com.kelab.cloud.user.model.User;
import com.kelab.cloud.user.repo.RoleRepository;
import com.kelab.cloud.user.repo.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;

        public AuthResponse register(RegisterRequest request) {

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("El email ya está registrado");
                }

                Role roleUser = roleRepository.findByName("ROLE_USER")
                                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

                User user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .tipoPersona(request.getTipoPersona())
                                .actorType(request.getActorType())
                                .afiliado(false)
                                .enabled(true)
                                .roles(Set.of(roleUser))
                                .build();

                userRepository.save(user);

                return AuthResponse.builder()
                                .message("Usuario registrado correctamente")
                                .email(user.getEmail())
                                .roles(Set.of("ROLE_USER"))
                                .actorType(user.getActorType())
                                .tipoPersona(user.getTipoPersona())
                                .build();
        }

        public AuthResponse login(LoginRequest request) {

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                String token = jwtService.generateToken(user);

                return AuthResponse.builder()
                                .token(token)
                                .email(user.getEmail())
                                .roles(
                                                user.getRoles()
                                                                .stream()
                                                                .map(Role::getName)
                                                                .collect(java.util.stream.Collectors.toSet()))
                                .actorType(user.getActorType())
                                .tipoPersona(user.getTipoPersona())
                                .message("Login exitoso")
                                .build();
        }

}
