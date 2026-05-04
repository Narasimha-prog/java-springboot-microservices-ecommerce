package com.sitamahalakshmi.review_service.service;

import com.sitamahalakshmi.review_service.dto.ReviewRequestDto;
import com.sitamahalakshmi.review_service.dto.ReviewResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IReviewService {
    ReviewResponseDto saveReview(ReviewRequestDto review, List<MultipartFile> files);

    List<ReviewResponseDto> getReviewsByProductId(UUID productId);

    ReviewResponseDto updateReview(UUID id, ReviewRequestDto reviewDetails);

    void deleteReview(UUID id);
}
