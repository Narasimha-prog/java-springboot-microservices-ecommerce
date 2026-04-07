package com.eswar.productservice.entity;

import com.eswar.productservice.audit.BaseEntity;
import com.eswar.productservice.constatnts.ProductColor;
import com.eswar.productservice.constatnts.ProductSize;
import com.eswar.productservice.constatnts.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.builder.HashCodeExclude;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
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

    @Column(nullable = false) // Added brand
    @ToString.Include
    private String brand;

    @ToString.Include
    @Enumerated(EnumType.STRING)
   private ProductSize productSize;



    @ToString.Include
    @Enumerated(EnumType.STRING)
    private ProductColor productColor;

    @Column(nullable = false) // Added featured
    @ToString.Include
    @Builder.Default
    private Boolean featured = false;

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

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<PictureEntity> pictureEntities=new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

@Transient
    public void addPicture(PictureEntity picture) {
        this.pictureEntities.add(picture);
        picture.setProduct(this);
    }
@Transient
    public void removePicture(PictureEntity picture) {
        this.pictureEntities.remove(picture);
        picture.setProduct(null);
    }

}