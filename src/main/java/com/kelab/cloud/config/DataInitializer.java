package com.kelab.cloud.config;

import com.kelab.cloud.user.model.Role;
import com.kelab.cloud.user.repo.RoleRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {

        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(
                    Role.builder()
                            .name("ROLE_USER")
                            .build());
        }

        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(
                    Role.builder()
                            .name("ROLE_ADMIN")
                            .build());
        }
    }
}
