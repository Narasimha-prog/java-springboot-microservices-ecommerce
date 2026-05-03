package com.sitamahalakshmi.review_service.dto;

public record ReviewRequestDto(
        String productId,
        String userId,
        int rating,
        String comment
) {}
