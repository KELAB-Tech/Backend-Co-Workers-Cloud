package com.kelab.cloud.user.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kelab.cloud.user.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
