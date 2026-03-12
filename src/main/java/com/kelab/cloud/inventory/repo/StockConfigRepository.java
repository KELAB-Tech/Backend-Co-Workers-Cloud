package com.kelab.cloud.inventory.repo;

import com.kelab.cloud.inventory.model.StockConfig;
import com.kelab.cloud.marketplace.model.Product;
import com.kelab.cloud.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockConfigRepository extends JpaRepository<StockConfig, Long> {

    Optional<StockConfig> findByProduct(Product product);

    // Todos los configs de productos de una tienda
    @Query("""
                SELECT sc FROM StockConfig sc
                WHERE sc.product.store = :store
            """)
    List<StockConfig> findByStore(@Param("store") Store store);
}