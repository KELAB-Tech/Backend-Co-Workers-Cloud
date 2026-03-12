package com.kelab.cloud.store.service;

import com.kelab.cloud.store.dto.StoreRequest;
import com.kelab.cloud.store.dto.StoreResponse;
import com.kelab.cloud.store.dto.StoreImageResponse;
import com.kelab.cloud.store.model.Store;
import com.kelab.cloud.store.model.StoreImage;
import com.kelab.cloud.store.model.StoreStatus;
import com.kelab.cloud.store.repo.StoreImageRepository;
import com.kelab.cloud.store.repo.StoreRepository;
import com.kelab.cloud.user.model.TipoPersona;
import com.kelab.cloud.user.model.User;
import com.kelab.cloud.user.repo.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreImageRepository storeImageRepository;

    // ==============================
    // ADMIN - Obtener tiendas por estado
    // ==============================
    public List<StoreResponse> getStoresByStatus(StoreStatus status) {

        return storeRepository
                .findByActiveTrueAndStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==============================
    // PUBLIC - List Approved Stores
    // ==============================
    public List<StoreResponse> getAllStores(String city, String name) {

        List<Store> stores;

        if (city != null && name != null) {
            stores = storeRepository.findByCityAndNameContainingIgnoreCaseAndActiveTrueAndStatus(
                    city, name, StoreStatus.APPROVED);
        } else if (city != null) {
            stores = storeRepository.findByCityAndActiveTrueAndStatus(
                    city, StoreStatus.APPROVED);
        } else if (name != null) {
            stores = storeRepository.findByNameContainingIgnoreCaseAndActiveTrueAndStatus(
                    name, StoreStatus.APPROVED);
        } else {
            stores = storeRepository.findByActiveTrueAndStatus(StoreStatus.APPROVED);
        }

        return stores.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==============================
    // PUBLIC - Get Store by ID
    // ==============================
    public StoreResponse getStoreById(Long id) {

        Store store = storeRepository.findById(id)
                .filter(Store::isActive)
                .filter(s -> s.getStatus() == StoreStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("Store no encontrada"));

        return mapToResponse(store);
    }

    // ==============================
    // COMPANY - Create Store
    // ==============================
    @Transactional
    public StoreResponse createStore(StoreRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getTipoPersona() != TipoPersona.JURIDICA) {
            throw new RuntimeException("Solo empresas pueden crear una tienda");
        }

        if (storeRepository.existsByOwner(user)) {
            throw new RuntimeException("La empresa ya tiene una tienda creada");
        }

        if (storeRepository.existsByName(request.getName().trim())) {
            throw new RuntimeException("Ya existe una tienda con ese nombre");
        }

        Store store = Store.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .phone(request.getPhone())
                .city(request.getCity().trim())
                .address(request.getAddress().trim())
                .logoUrl(request.getLogoUrl())
                .active(true)
                .status(StoreStatus.PENDING)
                .owner(user)
                .build();

        storeRepository.save(store);

        return mapToResponse(store);
    }

    // ==============================
    // OWNER - Update Store
    // ==============================
    @Transactional
    public StoreResponse updateStore(Long id, StoreRequest request, String email) {

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store no encontrada"));

        validateOwnership(store, email);

        store.setName(request.getName().trim());
        store.setDescription(request.getDescription());
        store.setPhone(request.getPhone());
        store.setCity(request.getCity().trim());
        store.setAddress(request.getAddress().trim());
        store.setLogoUrl(request.getLogoUrl());

        return mapToResponse(storeRepository.save(store));
    }

    // ==============================
    // OWNER - Soft Delete
    // ==============================
    @Transactional
    public void deleteStore(Long id, String email) {

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store no encontrada"));

        validateOwnership(store, email);

        store.setActive(false);
        storeRepository.save(store);
    }

    // ==============================
    // OWNER - My Store
    // ==============================
    public StoreResponse getMyStore(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new RuntimeException("No tienes tienda creada"));

        return mapToResponse(store);
    }

    // ==============================
    // ADMIN - Approve Store
    // ==============================
    @Transactional
    public StoreResponse approveStore(Long id) {

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store no encontrada"));

        if (store.getStatus() == StoreStatus.APPROVED) {
            throw new RuntimeException("La tienda ya está aprobada");
        }

        store.setStatus(StoreStatus.APPROVED);

        return mapToResponse(storeRepository.save(store));
    }

    // ==============================
    // ADMIN - Suspend Store
    // ==============================
    @Transactional
    public StoreResponse suspendStore(Long id) {

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store no encontrada"));

        if (store.getStatus() == StoreStatus.SUSPENDED) {
            throw new RuntimeException("La tienda ya está suspendida");
        }

        store.setStatus(StoreStatus.SUSPENDED);
        store.setActive(false); // 🔥 bloqueo institucional fuerte

        return mapToResponse(storeRepository.save(store));
    }

    // ==============================
    // IMAGES
    // ==============================

    @Transactional
    public StoreImageResponse addStoreImage(
            Long storeId,
            String imageUrl,
            String description,
            String email) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store no encontrada"));

        validateOwnership(store, email);

        StoreImage image = StoreImage.builder()
                .store(store)
                .imageUrl(imageUrl)
                .description(description)
                .active(true)
                .build();

        storeImageRepository.save(image);

        return mapImageToResponse(image);
    }

    public List<StoreImageResponse> getStoreImages(Long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store no encontrada"));

        return storeImageRepository.findByStoreAndActiveTrue(store)
                .stream()
                .map(this::mapImageToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteStoreImage(Long imageId, String email) {

        StoreImage image = storeImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));

        validateOwnership(image.getStore(), email);

        image.setActive(false);
        storeImageRepository.save(image);
    }

    // ==============================
    // PRIVATE HELPERS
    // ==============================

    private void validateOwnership(Store store, String email) {
        if (!store.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("No tienes permisos para esta operación");
        }
    }

    private StoreResponse mapToResponse(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .phone(store.getPhone())
                .city(store.getCity())
                .address(store.getAddress())
                .logoUrl(store.getLogoUrl())
                .status(store.getStatus())
                .active(store.isActive())
                .build();
    }

    private StoreImageResponse mapImageToResponse(StoreImage image) {
        return StoreImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .description(image.getDescription())
                .build();
    }
}