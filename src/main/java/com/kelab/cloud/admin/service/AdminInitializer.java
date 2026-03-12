package com.kelab.cloud.admin.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.kelab.cloud.user.model.ActorType;
import com.kelab.cloud.user.model.Role;
import com.kelab.cloud.user.model.TipoPersona;
import com.kelab.cloud.user.model.User;
import com.kelab.cloud.user.repo.RoleRepository;
import com.kelab.cloud.user.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name("ROLE_ADMIN")
                                .build()));

        if (userRepository.findByEmail("admin@cloud.com").isEmpty()) {

            User admin = User.builder()
                    .name("Administrador")
                    .email("admin@cloud.com")
                    .password(passwordEncoder.encode("Admin123"))
                    .tipoPersona(TipoPersona.JURIDICA)
                    .actorType(ActorType.ADMIN_GENERAL)
                    .enabled(true)
                    .build();

            admin.getRoles().add(adminRole);

            userRepository.save(admin);

            System.out.println("ADMIN CREADO ✅");
        }
    }
}
