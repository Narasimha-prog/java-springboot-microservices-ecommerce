package com.sitamahalakshmi.review_service.service;

import com.sitamahalakshmi.review_service.dto.ReviewRequestDto;
import com.sitamahalakshmi.review_service.entity.ReviewDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IReviewService {
    ReviewDocument saveReview(ReviewRequestDto review, List<MultipartFile> files);

    List<ReviewDocument> getReviewsByProductId(String productId);

    ReviewDocument updateReview(String id, ReviewDocument reviewDetails);

    void deleteReview(UUID id);
}
