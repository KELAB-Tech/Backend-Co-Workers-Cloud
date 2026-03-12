package com.kelab.cloud.marketplace.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kelab.cloud.marketplace.model.Product;
import com.kelab.cloud.marketplace.model.ProductImage;
import com.kelab.cloud.marketplace.model.ImageStatus;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductAndStatus(Product product, ImageStatus status);

}