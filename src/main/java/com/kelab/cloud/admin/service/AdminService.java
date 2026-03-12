package com.kelab.cloud.admin.service;

import com.kelab.cloud.admin.dto.AdminDashboardResponse;
import com.kelab.cloud.admin.dto.AdminProductSummaryResponse;
import com.kelab.cloud.admin.dto.AdminStoreProductSummaryResponse;
import com.kelab.cloud.marketplace.model.ProductStatus;
import com.kelab.cloud.marketplace.repo.ProductRepository;
import com.kelab.cloud.store.dto.StoreResponse;
import com.kelab.cloud.store.model.Store;
import com.kelab.cloud.store.model.StoreStatus;
import com.kelab.cloud.store.repo.StoreRepository;
import com.kelab.cloud.user.model.User;
import com.kelab.cloud.user.repo.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // ==============================
    // DASHBOARD
    // ==============================
    public AdminDashboardResponse getDashboard() {

        // ==============================
        // USERS
        // ==============================

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabledTrue();
        long suspendedUsers = userRepository.countByEnabledFalse();

        // ==============================
        // STORES
        // ==============================

        long totalStores = storeRepository.count();
        long approvedStores = storeRepository.countByStatus(StoreStatus.APPROVED);
        long pendingStores = storeRepository.countByStatus(StoreStatus.PENDING);
        long suspendedStores = storeRepository.countByStatus(StoreStatus.SUSPENDED);

        // ==============================
        // PRODUCTS
        // ==============================

        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByStatus(ProductStatus.ACTIVE);
        long inactiveProducts = productRepository.countByStatus(ProductStatus.INACTIVE);

        long featuredProducts = productRepository.countByFeaturedTrue();

        long outOfStockProducts = productRepository.countByStock(0);

        long lowStockProducts = productRepository.countByStockLessThan(5);

        long productsCreatedToday = productRepository.countByCreatedAtAfter(
                java.time.LocalDateTime.now().minusDays(1));

        // ==============================
        // LAST PRODUCTS
        // ==============================

        List<AdminProductSummaryResponse> lastProducts = productRepository
                .findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(p -> AdminProductSummaryResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .storeName(p.getStore().getName())
                        .price(p.getPrice())
                        .build())
                .toList();

        // ==============================
        // TOP STORES
        // ==============================

        List<AdminStoreProductSummaryResponse> topStores = storeRepository
                .findAll()
                .stream()
                .map(store -> AdminStoreProductSummaryResponse.builder()
                        .storeId(store.getId())
                        .storeName(store.getName())
                        .productCount(productRepository.countByStore(store))
                        .build())
                .sorted((a, b) -> Long.compare(b.getProductCount(), a.getProductCount()))
                .limit(5)
                .toList();

        // ==============================
        // RESPONSE
        // ==============================

        return AdminDashboardResponse.builder()

                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .suspendedUsers(suspendedUsers)

                .totalStores(totalStores)
                .approvedStores(approvedStores)
                .pendingStores(pendingStores)
                .suspendedStores(suspendedStores)

                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .inactiveProducts(inactiveProducts)
                .featuredProducts(featuredProducts)
                .outOfStockProducts(outOfStockProducts)
                .lowStockProducts(lowStockProducts)
                .productsCreatedToday(productsCreatedToday)

                .lastProducts(lastProducts)
                .topStores(topStores)

                .build();
    }

    // ==============================
    // STORES
    // ==============================

    public List<StoreResponse> getPendingStores() {

        return storeRepository
                .findByActiveTrueAndStatus(StoreStatus.PENDING)
                .stream()
                .map(this::mapStoreToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StoreResponse approveStore(Long id) {

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store no encontrada"));

        if (store.getStatus() == StoreStatus.APPROVED) {
            throw new RuntimeException("La tienda ya está aprobada");
        }

        store.setStatus(StoreStatus.APPROVED);

        return mapStoreToResponse(storeRepository.save(store));
    }

    // ==============================
    // USERS
    // ==============================

    @Transactional
    public void suspendUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setEnabled(false);

        if (user.getStore() != null) {

            Store store = user.getStore();

            if (store.getStatus() == StoreStatus.APPROVED) {
                store.setStatus(StoreStatus.SUSPENDED);
                storeRepository.save(store);
            }
        }

        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setEnabled(true);

        if (user.getStore() != null) {

            Store store = user.getStore();

            if (store.getStatus() == StoreStatus.SUSPENDED) {
                store.setStatus(StoreStatus.APPROVED);
                storeRepository.save(store);
            }
        }

        userRepository.save(user);
    }

    // ==============================
    // PRIVATE MAPPER
    // ==============================

    private StoreResponse mapStoreToResponse(Store store) {

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
}