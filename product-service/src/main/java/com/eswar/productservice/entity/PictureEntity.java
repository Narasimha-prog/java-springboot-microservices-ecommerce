package com.eswar.productservice.entity;

import com.eswar.productservice.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "product_picture")
@Getter
@Setter
@ToString(exclude = "product")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PictureEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "storage_key", nullable = false)
    private String storageKey; // Unique ID/Path in S3 or File System

    @Column(name = "file_name", nullable = false)
    private String fileName; // Original name (e.g., "iphone_15.png")

    @Column(name = "mime_type", nullable = false)
    private String mimeType; // e.g., "image/jpeg"

    @Column(name = "file_size")
    private Long fileSize; // Storing size in bytes is useful for UI/Validation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_fk", nullable = false)
    private ProductEntity product;
}
