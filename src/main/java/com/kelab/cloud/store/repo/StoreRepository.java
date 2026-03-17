package com.kelab.cloud.store.repo;

import com.kelab.cloud.store.model.Store;
import com.kelab.cloud.store.model.StoreStatus;
import com.kelab.cloud.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long>, JpaSpecificationExecutor<Store> {
        // Busca la tienda por el email del dueño — usado por /my-store
        Optional<Store> findByOwnerEmail(String email);

        // ==============================
        // OWNER
        // ==============================

        Optional<Store> findByOwner(User owner);

        boolean existsByOwner(User owner);

        // ==============================
        // VALIDATIONS
        // ==============================

        boolean existsByName(String name);

        // ==============================
        // ADMIN DASHBOARD
        // ==============================
        long countByStatus(StoreStatus status);

        long countByActiveTrue();

        // ==============================
        // PUBLIC FILTERS (APPROVED + ACTIVE)
        // ==============================

        List<Store> findByActiveTrueAndStatus(StoreStatus status);

        List<Store> findByCityAndActiveTrueAndStatus(String city, StoreStatus status);

        List<Store> findByNameContainingIgnoreCaseAndActiveTrueAndStatus(
                        String name, StoreStatus status);

        List<Store> findByCityAndNameContainingIgnoreCaseAndActiveTrueAndStatus(
                        String city,
                        String name,
                        StoreStatus status);
}