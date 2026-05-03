package com.sitamahalakshmi.review_service.service;

import com.sitamahalakshmi.review_service.dto.ReviewRequestDto;
import com.sitamahalakshmi.review_service.entity.ReviewDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
@Service
public class ReviewServiceImp implements IReviewService{


    @Override
    public ReviewDocument saveReview(ReviewRequestDto review, List<MultipartFile> files) {
        return null;
    }

    @Override
    public List<ReviewDocument> getReviewsByProductId(String productId) {
        return List.of();
    }

    @Override
    public ReviewDocument updateReview(String id, ReviewDocument reviewDetails) {
        return null;
    }

    @Override
    public void deleteReview(UUID id) {

    }
}
