package com.sitamahalakshmi.review_service.service;

import com.sitamahalakshmi.review_service.dto.ReviewRequestDto;
import com.sitamahalakshmi.review_service.dto.ReviewResponseDto;
import com.sitamahalakshmi.review_service.entity.ReviewEntity;
import com.sitamahalakshmi.review_service.entity.ReviewImageEntity;
import com.sitamahalakshmi.review_service.mapper.ReviewMapper;
import com.sitamahalakshmi.review_service.repository.IReviewRepository; // Assuming this exists
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImp implements IReviewService {

    private final IReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    // private final FileStorageService fileStorageService; // You'll need a service to save physical files

    @Override
    public ReviewResponseDto saveReview(ReviewRequestDto reviewDto, List<MultipartFile> files) {
        // 1. Convert DTO to Entity
        ReviewEntity review = reviewMapper.toEntity(reviewDto);
        review.setCreatedAt(Instant.now());

        // 2. Process Files (Logic depends on your File Storage implementation)
        if (files != null && !files.isEmpty()) {
            List<ReviewImageEntity> images = files.stream().map(file -> {
                // This is where you'd call your S3/Local storage logic
                // String storageKey = fileStorageService.save(file);
                return ReviewImageEntity.builder()
                        .fileName(file.getOriginalFilename())
                        .mimeType(file.getContentType())
                        .fileSize(file.getSize())
                        .storageKey("placeholder-key-" + UUID.randomUUID()) // Replace with actual key from storage
                        .build();
            }).collect(Collectors.toList());

            review.setImages(images);
        }

        // 3. Save to MongoDB
        ReviewEntity savedReview = reviewRepository.save(review);

        // 4. Return Response DTO
        return reviewMapper.toResponse(savedReview);
    }

    @Override
    public List<ReviewResponseDto> getReviewsByProductId(UUID productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewResponseDto updateReview(UUID id, ReviewRequestDto reviewDetails) {
        ReviewEntity  existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));

        // Update fields
        existingReview.setRating(reviewDetails.rating());
        existingReview.setComment(reviewDetails.comment());
        // images are usually handled via separate upload/delete endpoints to save bandwidth

        return reviewMapper.toResponse(reviewRepository.save(existingReview));
    }

    @Override
    public void deleteReview(UUID id) {
        // Since MongoDB uses String IDs usually, we convert UUID to String
        reviewRepository.deleteById(id);
    }
}