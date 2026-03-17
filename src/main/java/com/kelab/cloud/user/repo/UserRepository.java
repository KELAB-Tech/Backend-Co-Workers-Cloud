package com.kelab.cloud.user.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kelab.cloud.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // ==============================
    // VALIDATIONS
    long countByEnabledTrue();

    // ==============================
    // ADMIN DASHBOARD
    long countByEnabledFalse();

    // ==============================
    // LOGIN & REGISTRATION
    Optional<User> findByEmail(String email);

    // ==============================
    // PUBLIC
    boolean existsByEmail(String email);

    // ==============================
    // AFILIADOS
    List<User> findByAfiliadoTrue();
}
