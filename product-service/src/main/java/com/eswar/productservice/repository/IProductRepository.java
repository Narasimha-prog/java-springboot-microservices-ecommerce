package com.eswar.productservice.repository;

import com.eswar.productservice.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IProductRepository extends JpaRepository<ProductEntity, UUID> {

    boolean existsBySku(String sku);

    Page<ProductEntity> findByFeaturedTrue(Pageable pageable);

    Page<ProductEntity> findByCategoryId(UUID categoryId, Pageable pageable);

    // Finds products in the same category but excludes the current product
    Page<ProductEntity> findByCategoryIdAndIdNot(UUID categoryId, UUID productId, Pageable pageable);

    // Finds products by category and a list of sizes
    Page<ProductEntity> findByCategoryIdAndProductSizeIn(UUID categoryId, List<String> sizes, Pageable pageable);
}
