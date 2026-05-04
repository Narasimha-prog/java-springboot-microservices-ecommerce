package com.sitamahalakshmi.review_service.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

/**
 * In MongoDB, if images are small and specific to a review,
 * this class doesn't necessarily need @Document if it's stored as a
 * list inside ReviewEntity.
 *
 * However, if you want to query images independently, keep @Document.
 */
@Document(collection = "review_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImageEntity {

    @Id
    private String id; // MongoDB traditionally uses String or ObjectId

    @Field("storage_path")
    private String storagePath;

    @Field("storage_key")
    private String storageKey;

    @Field("file_name")
    private String fileName;

    @Field("mime_type")
    private String mimeType;

    @Field("file_size")
    private Long fileSize;

    // In MongoDB, we usually store the ID of the parent rather than the full object
    @Field("review_id")
    private String reviewId;
}