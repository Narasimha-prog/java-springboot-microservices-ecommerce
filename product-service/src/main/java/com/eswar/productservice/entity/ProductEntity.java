package com.eswar.productservice.entity;

import com.eswar.productservice.audit.BaseEntity;
import com.eswar.productservice.constatnts.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.builder.HashCodeExclude;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(name = "products")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true,callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProductEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, unique = true)
    @ToString.Include
    private String sku;

    @Column(nullable = false)
    @ToString.Include
    private String name;

    @Column(length = 1000)
    @ToString.Include
    private String description;

    @Column(nullable = false)
    @ToString.Include
    @Builder.Default
    private BigDecimal price= BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private ProductStatus status;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

}