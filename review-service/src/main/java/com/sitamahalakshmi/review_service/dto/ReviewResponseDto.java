package com.sitamahalakshmi.review_service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReviewResponseDto(
        UUID id,
        UUID productId,
        UUID userId,
        int rating,
        String comment,
        List<String> imageUrls,
        Instant createdAt
) {}
