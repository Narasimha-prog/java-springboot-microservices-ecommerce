package com.sitamahalakshmi.review_service.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.util.UUID;
import java.time.Instant;

@Document(collection = "reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReviewDocument {

    @Id
    private String id; // MongoDB's internal ID (usually a String or ObjectId)

    @Indexed // Indexing this makes searching for reviews by product very fast
    private UUID productId;

    @Indexed
    private UUID userId;

    private String userFullName;

    private int rating;

    private String comment;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}