package com.sitamahalakshmi.review_service.repository;

import com.sitamahalakshmi.review_service.entity.ReviewEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface IReviewRepository extends MongoRepository<ReviewEntity, UUID> {

    List<ReviewEntity> findByProductId(UUID productId);
}
