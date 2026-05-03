package com.sitamahalakshmi.review_service.rest;


import com.sitamahalakshmi.review_service.dto.ReviewRequestDto;
import com.sitamahalakshmi.review_service.entity.ReviewDocument;
import com.sitamahalakshmi.review_service.service.IReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "reviews", description = "API for managing users reviews  in the e-commerce platform")
public class ReviewRestController {

    private final IReviewService reviewService;

    // 1. CREATE

    @Operation(summary = "Post a new review")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewDocument> createReview(@RequestPart("review") ReviewRequestDto dto,
                                                       @RequestPart("files") List<MultipartFile> files) {
        return new ResponseEntity<>(reviewService.saveReview(dto,files), HttpStatus.CREATED);
    }

    // 2. READ (By Product)
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews for a product")
    public ResponseEntity<List<ReviewDocument>> getReviewsByProduct(@PathVariable String productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProductId(productId));
    }

    // 3. UPDATE
    @PutMapping("/{id}")
    @Operation(summary = "Edit an existing review")
    public ResponseEntity<ReviewDocument> updateReview(
            @PathVariable String id,
            @RequestBody ReviewDocument reviewDetails) {
        return ResponseEntity.ok(reviewService.updateReview(id, reviewDetails));
    }

    // 4. DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a review")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
