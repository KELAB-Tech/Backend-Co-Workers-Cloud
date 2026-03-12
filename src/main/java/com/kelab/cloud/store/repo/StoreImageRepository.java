package com.kelab.cloud.store.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kelab.cloud.store.model.Store;
import com.kelab.cloud.store.model.StoreImage;

import java.util.List;

public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {

    List<StoreImage> findByStoreAndActiveTrue(Store store);
}