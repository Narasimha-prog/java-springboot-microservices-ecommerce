package com.eswar.productservice.repository;

import com.eswar.productservice.constatnts.ProductSize;
import com.eswar.productservice.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IProductRepository extends JpaRepository<ProductEntity, UUID> {

    boolean existsBySku(String sku);

    Page<ProductEntity> findByFeaturedTrue(Pageable pageable);

    Page<ProductEntity> findByCategoryId(UUID categoryId, Pageable pageable);

    @Query("""
    SELECT p FROM ProductEntity p 
    WHERE p.category.id = :categoryId 
    AND p.id <> :productId
""")
    Page<ProductEntity> findByCategoryIdAndIdNot(
            @Param("categoryId") UUID categoryId,
            @Param("productId") UUID productId,
            Pageable pageable
    );

    // Finds products by category and a list of sizes
    Page<ProductEntity> findByCategoryIdAndProductSizeIn(UUID categoryId, List<ProductSize> sizes, Pageable pageable);
}
